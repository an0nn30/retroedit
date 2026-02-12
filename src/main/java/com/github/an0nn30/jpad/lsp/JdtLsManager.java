package com.github.an0nn30.jpad.lsp;

import com.github.an0nn30.jpad.logging.Logger;
import com.github.an0nn30.jpad.settings.Settings;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.stream.Stream;

public class JdtLsManager {

    private static final String JDTLS_VERSION = "1.40.0";
    private static final String JDTLS_TIMESTAMP = "202409261450";
    private static final String JDTLS_DOWNLOAD_URL =
            "https://download.eclipse.org/jdtls/milestones/" + JDTLS_VERSION +
            "/jdt-language-server-" + JDTLS_VERSION + "-" + JDTLS_TIMESTAMP + ".tar.gz";

    private final File jdtlsDir;

    public JdtLsManager() {
        String customPath = Settings.getInstance().getJdtlsPath();
        if (customPath != null && !customPath.isEmpty()) {
            this.jdtlsDir = new File(customPath);
        } else {
            this.jdtlsDir = Settings.getSettingsFilePath("jdtls").toFile();
        }
    }

    public boolean isInstalled() {
        return findLauncherJar() != null;
    }

    public void ensureInstalled() throws IOException {
        if (isInstalled()) {
            Logger.getInstance().info(JdtLsManager.class, "JDT LS already installed at " + jdtlsDir);
            return;
        }

        Logger.getInstance().info(JdtLsManager.class, "Downloading JDT LS from " + JDTLS_DOWNLOAD_URL);
        Files.createDirectories(jdtlsDir.toPath());

        File tarball = new File(jdtlsDir, "jdtls.tar.gz");
        try (InputStream in = new URL(JDTLS_DOWNLOAD_URL).openStream();
             OutputStream out = new FileOutputStream(tarball)) {
            in.transferTo(out);
        }

        // Extract using tar
        ProcessBuilder pb = new ProcessBuilder("tar", "xzf", tarball.getAbsolutePath());
        pb.directory(jdtlsDir);
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        try (var reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            while (reader.readLine() != null) { /* drain */ }
        }
        int exit;
        try {
            exit = proc.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("JDT LS extraction interrupted", e);
        }
        if (exit != 0) {
            throw new IOException("Failed to extract JDT LS tarball, exit code: " + exit);
        }
        tarball.delete();

        if (!isInstalled()) {
            throw new IOException("JDT LS installation failed - launcher jar not found in " + jdtlsDir);
        }
        Logger.getInstance().info(JdtLsManager.class, "JDT LS installed successfully");
    }

    public File findLauncherJar() {
        File pluginsDir = new File(jdtlsDir, "plugins");
        if (!pluginsDir.isDirectory()) return null;

        try (Stream<Path> paths = Files.list(pluginsDir.toPath())) {
            return paths
                    .map(Path::toFile)
                    .filter(f -> f.getName().startsWith("org.eclipse.equinox.launcher_") && f.getName().endsWith(".jar"))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    public File getConfigDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String configName;
        if (os.contains("mac")) {
            configName = "config_mac";
        } else if (os.contains("win")) {
            configName = "config_win";
        } else {
            configName = "config_linux";
        }
        return new File(jdtlsDir, configName);
    }

    public File getWorkspaceDataDir(File workspaceRoot) {
        String hash = Integer.toHexString(workspaceRoot.getAbsolutePath().hashCode());
        File dataDir = new File(jdtlsDir, "workspace-data/" + hash);
        try {
            Files.createDirectories(dataDir.toPath());
        } catch (IOException e) {
            Logger.getInstance().warn(JdtLsManager.class, "Failed to create workspace data dir: " + e.getMessage());
        }
        return dataDir;
    }
}
