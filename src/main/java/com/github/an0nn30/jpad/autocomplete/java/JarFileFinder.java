package com.github.an0nn30.jpad.autocomplete.java;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class JarFileFinder {

    /**
     * Searches for jar files starting from the given directory.
     * It gathers jars by:
     *  - Recursively scanning the provided directory
     *  - Scanning the current class path
     *  - Searching the Java installation's lib directory
     *
     * @param currentDir the base directory to scan
     * @return a set of jar files found
     * @throws IOException if an I/O error occurs during scanning
     */
    public Set<File> findJarFiles(File currentDir) throws IOException {
        Set<File> jarFiles = new HashSet<>();

        // 1. Recursively search the current directory.
        addJarFilesRecursively(currentDir, jarFiles);

        // 2. Scan jar files from the class path.
        String classPath = System.getProperty("java.class.path");
        if (classPath != null && !classPath.isEmpty()) {
            String[] classPathEntries = classPath.split(File.pathSeparator);
            for (String entry : classPathEntries) {
                File file = new File(entry);
                if (file.isFile() && entry.toLowerCase().endsWith(".jar")) {
                    jarFiles.add(file.getAbsoluteFile());
                }
            }
        }

        // 3. Scan the Java installation's lib directory.
        File javaHome = new File(System.getProperty("java.home"));
        File libDir = new File(javaHome, "lib");
        if (libDir.exists() && libDir.isDirectory()) {
            addJarFilesRecursively(libDir, jarFiles);
        }

        return jarFiles;
    }

    private void addJarFilesRecursively(File directory, Set<File> jarFiles) throws IOException {
        if (directory == null || !directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                addJarFilesRecursively(file, jarFiles);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                jarFiles.add(file.getAbsoluteFile());
            }
        }
    }
}