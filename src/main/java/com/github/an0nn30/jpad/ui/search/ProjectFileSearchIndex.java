package com.github.an0nn30.jpad.ui.search;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectFileSearchIndex {

    private static List<File> indexedFiles = new ArrayList<>();
    private static boolean indexingInProgress = false;
    private static List<String> gitIgnoreRegexes = new ArrayList<>();

    /**
     * Starts the asynchronous indexing of the project folder.
     *
     * @param root the project root directory
     */
    public static void buildIndex(File root) {
        if (root == null || !root.isDirectory()) {
            return;
        }
        indexingInProgress = true;
        System.out.println("Starting indexing of project directory: " + root.getAbsolutePath());

        new SwingWorker<List<File>, String>() {
            @Override
            protected List<File> doInBackground() {
                List<File> files = new ArrayList<>();
                // If this is a Git repository, load .gitignore patterns.
                File gitDir = new File(root, ".git");
                if (gitDir.exists() && gitDir.isDirectory()) {
                    File gitignoreFile = new File(root, ".gitignore");
                    if (gitignoreFile.exists() && gitignoreFile.isFile()) {
                        gitIgnoreRegexes = parseGitIgnore(gitignoreFile);
                        publish("Loaded .gitignore patterns.");
                    }
                }
                traverseDirectory(root, root, files);
                return files;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    System.out.println(msg);
                }
            }

            @Override
            protected void done() {
                try {
                    indexedFiles = get();
                    System.out.println("Indexing complete. " + indexedFiles.size() + " files indexed.");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    indexingInProgress = false;
                }
            }
        }.execute();
    }

    private static void traverseDirectory(File dir, File root, List<File> files) {
        File[] list = dir.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) {
                // Skip the .git directory entirely.
                if (f.getName().equalsIgnoreCase(".git")) {
                    System.out.println("Skipping .git directory: " + f.getAbsolutePath());
                    continue;
                }
                traverseDirectory(f, root, files);
            } else {
                if (!isBinaryFile(f) && !isIgnoredByGit(f, root)) {
                    files.add(f);
                    System.out.println("Indexed file: " + f.getAbsolutePath());
                }
            }
        }
    }

    private static boolean isBinaryFile(File file) {
        String name = file.getName().toLowerCase();
        String[] binaryExtensions = { ".exe", ".dll", ".so", ".bin", ".class", ".jar",
                ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".ico",
                ".pdf", ".zip", ".tar", ".gz", ".7z", ".mp3", ".mp4",
                ".avi", ".mov", ".wmv" };
        for (String ext : binaryExtensions) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIgnoredByGit(File file, File root) {
        if (gitIgnoreRegexes.isEmpty()) {
            return false;
        }
        String relativePath = file.getAbsolutePath()
                .substring(root.getAbsolutePath().length() + 1)
                .replace(File.separatorChar, '/');
        for (String regex : gitIgnoreRegexes) {
            if (relativePath.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> parseGitIgnore(File gitignoreFile) {
        List<String> regexes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gitignoreFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Skip blank lines or comments.
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String regex = gitIgnorePatternToRegex(line);
                regexes.add(regex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return regexes;
    }

    /**
     * Converts a simple .gitignore glob pattern to a regex.
     * This is a basic conversion and may not cover the full .gitignore syntax.
     */
    private static String gitIgnorePatternToRegex(String pattern) {
        if (pattern.endsWith("/")) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        pattern = pattern.replace(".", "\\.");
        pattern = pattern.replace("**", ".*");
        pattern = pattern.replace("*", "[^/]*");
        if (pattern.startsWith("/")) {
            pattern = pattern.substring(1);
        }
        return ".*" + pattern + ".*";
    }

    public static List<File> getIndexedFiles() {
        return indexedFiles;
    }

    public static boolean isIndexingInProgress() {
        return indexingInProgress;
    }
}