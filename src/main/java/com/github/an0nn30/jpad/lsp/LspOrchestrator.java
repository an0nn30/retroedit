package com.github.an0nn30.jpad.lsp;

import com.github.an0nn30.jpad.event.EventBus;
import com.github.an0nn30.jpad.event.EventType;
import com.github.an0nn30.jpad.logging.Logger;
import com.github.an0nn30.jpad.lsp.completion.LspAutoCompletionInstaller;
import com.github.an0nn30.jpad.lsp.diagnostics.DiagnosticsManager;
import com.github.an0nn30.jpad.lsp.features.GoToDefinitionHandler;
import com.github.an0nn30.jpad.lsp.features.HoverProvider;
import com.github.an0nn30.jpad.project.ProjectManager;
import com.github.an0nn30.jpad.settings.Settings;
import com.github.an0nn30.jpad.ui.TextAreaTabManager;
import com.github.an0nn30.jpad.ui.components.TextArea;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LspOrchestrator {

    private final ProjectManager projectManager;
    private final JdtLsManager jdtLsManager;
    private final LspClient lspClient;
    private final DocumentSyncManager documentSyncManager;
    private final DiagnosticsManager diagnosticsManager;
    private final TextAreaTabManager tabManager;

    // Track installed features per file URI to avoid double-install
    private final Map<String, InstalledFeatures> installedFeatures = new ConcurrentHashMap<>();

    public LspOrchestrator(TextAreaTabManager tabManager) {
        this.tabManager = tabManager;
        this.projectManager = new ProjectManager();
        this.jdtLsManager = new JdtLsManager();
        this.lspClient = new LspClient(jdtLsManager);
        this.documentSyncManager = new DocumentSyncManager(lspClient);
        this.diagnosticsManager = new DiagnosticsManager(lspClient);

        // When LSP becomes READY, install features on the currently active tab
        EventBus.subscribe(EventType.LSP_STATUS_CHANGED.name(), event -> {
            if (event.data() instanceof LspClient.Status status && status == LspClient.Status.READY) {
                SwingUtilities.invokeLater(() -> {
                    TextArea activeTextArea = tabManager.getActiveTextArea();
                    if (activeTextArea != null) {
                        installLspFeatures(activeTextArea);
                    }
                });
            }
        });
    }

    public void onWorkspaceOpened(File rootDir) {
        if (!Settings.getInstance().isLspEnabled()) {
            Logger.getInstance().info(LspOrchestrator.class, "LSP disabled in settings");
            return;
        }

        // Start project detection and LSP in background
        projectManager.loadProject(rootDir).thenRun(() -> {
            if (lspClient.getStatus() == LspClient.Status.NOT_STARTED
                    || lspClient.getStatus() == LspClient.Status.ERROR) {
                lspClient.start(rootDir);
            }
        });
    }

    public void installLspFeatures(TextArea textArea) {
        if (!Settings.getInstance().isLspEnabled()) {
            Logger.getInstance().debug(LspOrchestrator.class, "installLspFeatures: LSP disabled");
            return;
        }
        if (lspClient.getStatus() != LspClient.Status.READY) {
            Logger.getInstance().debug(LspOrchestrator.class, "installLspFeatures: LSP not ready (status=" + lspClient.getStatus() + ")");
            return;
        }

        File file = textArea.getActiveFile();
        if (file == null) {
            Logger.getInstance().debug(LspOrchestrator.class, "installLspFeatures: no active file");
            return;
        }

        String style = textArea.getSyntaxEditingStyle();
        if (!SyntaxConstants.SYNTAX_STYLE_JAVA.equals(style)) {
            Logger.getInstance().debug(LspOrchestrator.class, "installLspFeatures: not Java (style=" + style + ")");
            return;
        }

        String uri = file.toURI().toString();

        // Skip if already installed for this URI
        if (installedFeatures.containsKey(uri)) {
            diagnosticsManager.setActiveDocument(textArea, uri);
            return;
        }

        Logger.getInstance().info(LspOrchestrator.class, "Installing LSP features for " + file.getName());

        // Uninstall any existing JavaLanguageSupport to avoid competing AutoCompletion
        try {
            LanguageSupport jls = LanguageSupportFactory.get().getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVA);
            if (jls != null) {
                jls.uninstall(textArea);
            }
        } catch (Exception e) {
            Logger.getInstance().debug(LspOrchestrator.class, "Could not uninstall JavaLanguageSupport: " + e.getMessage());
        }

        // Document sync
        documentSyncManager.documentOpened(textArea, file);

        // Completion
        AutoCompletion ac = LspAutoCompletionInstaller.install(textArea, lspClient, file, documentSyncManager);

        // Diagnostics
        diagnosticsManager.installHighlighter(textArea, uri);

        // Hover
        HoverProvider hoverProvider = new HoverProvider(lspClient, textArea, file);
        hoverProvider.install();

        // Go-to-definition
        GoToDefinitionHandler gotoHandler = new GoToDefinitionHandler(lspClient, textArea, file, tabManager);
        gotoHandler.install();

        installedFeatures.put(uri, new InstalledFeatures(ac, hoverProvider, gotoHandler));
    }

    public void onFileSaved(TextArea textArea, File file) {
        if (file == null || lspClient.getStatus() != LspClient.Status.READY) return;

        String style = textArea.getSyntaxEditingStyle();
        if (SyntaxConstants.SYNTAX_STYLE_JAVA.equals(style)) {
            documentSyncManager.documentSaved(textArea, file);
        }
    }

    public void onTabClosed(TextArea textArea, File file) {
        if (file == null) return;

        String uri = file.toURI().toString();
        InstalledFeatures features = installedFeatures.remove(uri);
        if (features != null) {
            features.uninstall();
        }

        if (lspClient.getStatus() == LspClient.Status.READY) {
            documentSyncManager.documentClosed(textArea, file);
        }
        diagnosticsManager.removeHighlighter(uri);
    }

    public void shutdown() {
        // Uninstall all features
        installedFeatures.values().forEach(InstalledFeatures::uninstall);
        installedFeatures.clear();

        documentSyncManager.shutdown();
        diagnosticsManager.shutdown();
        lspClient.shutdown();
    }

    public LspClient getLspClient() {
        return lspClient;
    }

    public boolean isLspReady() {
        return Settings.getInstance().isLspEnabled()
                && lspClient.getStatus() == LspClient.Status.READY;
    }

    private static class InstalledFeatures {
        final AutoCompletion autoCompletion;
        final HoverProvider hoverProvider;
        final GoToDefinitionHandler goToDefinitionHandler;

        InstalledFeatures(AutoCompletion ac, HoverProvider hover, GoToDefinitionHandler gotoDef) {
            this.autoCompletion = ac;
            this.hoverProvider = hover;
            this.goToDefinitionHandler = gotoDef;
        }

        void uninstall() {
            if (autoCompletion != null) autoCompletion.uninstall();
            if (hoverProvider != null) hoverProvider.uninstall();
            if (goToDefinitionHandler != null) goToDefinitionHandler.uninstall();
        }
    }
}
