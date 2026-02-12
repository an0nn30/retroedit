package com.github.an0nn30.jpad.lsp;

import com.github.an0nn30.jpad.event.EventBus;
import com.github.an0nn30.jpad.event.EventType;
import com.github.an0nn30.jpad.logging.Logger;
import com.google.gson.JsonObject;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class LspClient implements LanguageClient {

    public enum Status {
        NOT_STARTED, STARTING, READY, ERROR, SHUTTING_DOWN
    }

    private volatile Status status = Status.NOT_STARTED;
    private LanguageServer server;
    private Process process;
    private Future<?> listening;
    private final ConcurrentHashMap<String, List<Diagnostic>> diagnostics = new ConcurrentHashMap<>();
    private final JdtLsManager jdtLsManager;

    public LspClient(JdtLsManager jdtLsManager) {
        this.jdtLsManager = jdtLsManager;
    }

    public Status getStatus() {
        return status;
    }

    public LanguageServer getServer() {
        return server;
    }

    public List<Diagnostic> getDiagnostics(String uri) {
        return diagnostics.getOrDefault(uri, Collections.emptyList());
    }

    public void start(File workspaceRoot) {
        if (status != Status.NOT_STARTED && status != Status.ERROR) {
            return;
        }
        setStatus(Status.STARTING);

        try {
            jdtLsManager.ensureInstalled();
            File launcherJar = jdtLsManager.findLauncherJar();
            if (launcherJar == null) {
                throw new IllegalStateException("JDT LS launcher jar not found");
            }

            File configDir = jdtLsManager.getConfigDir();
            File dataDir = jdtLsManager.getWorkspaceDataDir(workspaceRoot);

            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-Declipse.application=org.eclipse.jdt.ls.core.id1");
            command.add("-Dosgi.bundles.defaultStartLevel=4");
            command.add("-Declipse.product=org.eclipse.jdt.ls.core.product");
            command.add("-Xmx1G");
            command.add("--add-modules=ALL-SYSTEM");
            command.add("-jar");
            command.add(launcherJar.getAbsolutePath());
            command.add("-configuration");
            command.add(configDir.getAbsolutePath());
            command.add("-data");
            command.add(dataDir.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            process = pb.start();

            Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(
                    this,
                    process.getInputStream(),
                    process.getOutputStream()
            );

            server = launcher.getRemoteProxy();
            listening = launcher.startListening();

            // Drain stderr in background
            Thread stderrThread = new Thread(() -> {
                try (var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Logger.getInstance().debug(LspClient.class, "[JDT LS stderr] " + line);
                    }
                } catch (Exception e) {
                    // process ended
                }
            }, "jdtls-stderr-reader");
            stderrThread.setDaemon(true);
            stderrThread.start();

            // Send initialize
            InitializeParams initParams = buildInitializeParams(workspaceRoot);
            server.initialize(initParams).thenAccept(result -> {
                server.initialized(new InitializedParams());
                setStatus(Status.READY);
                Logger.getInstance().info(LspClient.class, "JDT LS initialized successfully");
            }).exceptionally(ex -> {
                Logger.getInstance().error(LspClient.class, "JDT LS initialization failed: " + ex.getMessage());
                setStatus(Status.ERROR);
                return null;
            });

        } catch (Exception e) {
            Logger.getInstance().error(LspClient.class, "Failed to start JDT LS: " + e.getMessage());
            setStatus(Status.ERROR);
        }
    }

    public void shutdown() {
        if (status == Status.SHUTTING_DOWN || status == Status.NOT_STARTED) {
            return;
        }
        setStatus(Status.SHUTTING_DOWN);

        try {
            if (server != null) {
                server.shutdown().get(5, TimeUnit.SECONDS);
                server.exit();
            }
        } catch (Exception e) {
            Logger.getInstance().warn(LspClient.class, "LSP shutdown error: " + e.getMessage());
        }

        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }

        if (listening != null) {
            listening.cancel(true);
        }

        setStatus(Status.NOT_STARTED);
    }

    private void setStatus(Status newStatus) {
        Status old = this.status;
        this.status = newStatus;
        EventBus.publish(EventType.LSP_STATUS_CHANGED.name(), newStatus, this);
        Logger.getInstance().info(LspClient.class, "LSP status: " + old + " -> " + newStatus);
    }

    private InitializeParams buildInitializeParams(File workspaceRoot) {
        InitializeParams params = new InitializeParams();
        params.setRootUri(workspaceRoot.toURI().toString());

        WorkspaceFolder folder = new WorkspaceFolder();
        folder.setUri(workspaceRoot.toURI().toString());
        folder.setName(workspaceRoot.getName());
        params.setWorkspaceFolders(List.of(folder));

        // Client capabilities
        ClientCapabilities capabilities = new ClientCapabilities();

        // Text document capabilities
        TextDocumentClientCapabilities textDoc = new TextDocumentClientCapabilities();

        // Completion capabilities
        CompletionCapabilities completion = new CompletionCapabilities();
        CompletionItemCapabilities completionItem = new CompletionItemCapabilities();
        completionItem.setSnippetSupport(true);
        completionItem.setDocumentationFormat(List.of("plaintext", "markdown"));
        completion.setCompletionItem(completionItem);
        textDoc.setCompletion(completion);

        HoverCapabilities hover = new HoverCapabilities();
        textDoc.setHover(hover);

        DefinitionCapabilities definition = new DefinitionCapabilities();
        textDoc.setDefinition(definition);

        // Synchronization capabilities — tell server we support didOpen/didChange/didSave
        SynchronizationCapabilities sync = new SynchronizationCapabilities();
        sync.setDidSave(true);
        sync.setWillSave(false);
        sync.setWillSaveWaitUntil(false);
        textDoc.setSynchronization(sync);

        capabilities.setTextDocument(textDoc);

        // Workspace capabilities
        WorkspaceClientCapabilities workspace = new WorkspaceClientCapabilities();
        workspace.setWorkspaceFolders(true);
        capabilities.setWorkspace(workspace);

        params.setCapabilities(capabilities);

        // JDT-specific initialization options for Maven/Gradle auto-import
        Map<String, Object> initOptions = new HashMap<>();
        Map<String, Object> settings = new HashMap<>();
        Map<String, Object> java = new HashMap<>();
        Map<String, Object> importSettings = new HashMap<>();
        Map<String, Object> maven = new HashMap<>();
        Map<String, Object> gradle = new HashMap<>();

        maven.put("enabled", true);
        gradle.put("enabled", true);
        importSettings.put("maven", maven);
        importSettings.put("gradle", gradle);
        java.put("import", importSettings);
        settings.put("java", java);
        initOptions.put("settings", settings);

        params.setInitializationOptions(initOptions);

        return params;
    }

    // LanguageClient interface implementations

    @Override
    public void telemetryEvent(Object object) {
        // no-op
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams params) {
        String uri = params.getUri();
        List<Diagnostic> diags = params.getDiagnostics();
        if (diags.isEmpty()) {
            diagnostics.remove(uri);
        } else {
            diagnostics.put(uri, new ArrayList<>(diags));
        }
        EventBus.publish(EventType.LSP_DIAGNOSTICS.name(), uri, this);
    }

    @Override
    public void showMessage(MessageParams messageParams) {
        Logger.getInstance().info(LspClient.class,
                "[JDT LS] " + messageParams.getType() + ": " + messageParams.getMessage());
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams params) {
        Logger.getInstance().info(LspClient.class, "[JDT LS Request] " + params.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void logMessage(MessageParams message) {
        Logger.getInstance().debug(LspClient.class, "[JDT LS Log] " + message.getMessage());
    }

    @Override
    public CompletableFuture<Void> registerCapability(RegistrationParams params) {
        // JDT LS registers capabilities dynamically; accept them all.
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
        return CompletableFuture.completedFuture(null);
    }

    @JsonNotification("language/status")
    public void languageStatus(Object params) {
        // JDT LS sends {type: "Starting"|"Message"|"Started", message: "..."}
        String type = "";
        String message = "";
        if (params instanceof JsonObject json) {
            type = json.has("type") ? json.get("type").getAsString() : "";
            message = json.has("message") ? json.get("message").getAsString() : "";
        } else if (params instanceof Map<?,?> map) {
            Object t = map.get("type");
            Object m = map.get("message");
            type = t != null ? String.valueOf(t) : "";
            message = m != null ? String.valueOf(m) : "";
        }
        Logger.getInstance().debug(LspClient.class, "[JDT LS Status] " + type + ": " + message);
        EventBus.publish(EventType.LSP_LANGUAGE_STATUS.name(), new LanguageStatusInfo(type, message), this);
    }

    public record LanguageStatusInfo(String type, String message) {}
}
