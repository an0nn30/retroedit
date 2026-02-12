package com.github.an0nn30.jpad.project;

import com.github.an0nn30.jpad.logging.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GradleProject implements JavaProject {

    private final File projectRoot;
    private List<File> classpath = new ArrayList<>();
    private List<String> modules = new ArrayList<>();
    private String sourceLevel = "17";
    private boolean resolved = false;

    public GradleProject(File projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public File getProjectRoot() {
        return projectRoot;
    }

    @Override
    public String getBuildSystem() {
        return "gradle";
    }

    @Override
    public List<File> getSourceRoots() {
        List<File> roots = new ArrayList<>();
        File mainJava = new File(projectRoot, "src/main/java");
        File testJava = new File(projectRoot, "src/test/java");
        if (mainJava.isDirectory()) roots.add(mainJava);
        if (testJava.isDirectory()) roots.add(testJava);
        return roots;
    }

    @Override
    public List<File> getClasspath() {
        return classpath;
    }

    @Override
    public File getOutputDirectory() {
        return new File(projectRoot, "build/classes/java/main");
    }

    @Override
    public String getSourceLevel() {
        return sourceLevel;
    }

    @Override
    public void resolveDependencies() throws Exception {
        resolveClasspath();
        resolved = true;
    }

    @Override
    public List<String> getModules() {
        return modules;
    }

    @Override
    public boolean isResolved() {
        return resolved;
    }

    private void resolveClasspath() {
        try {
            File initScript = Files.createTempFile("gradle-cp-", ".gradle").toFile();
            initScript.deleteOnExit();

            Files.writeString(initScript.toPath(),
                    "allprojects {\n" +
                    "    task printClasspath {\n" +
                    "        doLast {\n" +
                    "            try {\n" +
                    "                configurations.compileClasspath.files.each { println it }\n" +
                    "            } catch (Exception e) {\n" +
                    "                // configuration not found\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n"
            );

            // Prefer gradlew if available
            String gradleCmd = "gradle";
            File gradlew = new File(projectRoot, "gradlew");
            if (gradlew.exists() && gradlew.canExecute()) {
                gradleCmd = gradlew.getAbsolutePath();
            }

            ProcessBuilder pb = new ProcessBuilder(
                    gradleCmd, "--init-script", initScript.getAbsolutePath(),
                    "printClasspath", "-q"
            );
            pb.directory(projectRoot);
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            List<String> lines = new ArrayList<>();
            try (var reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        lines.add(line);
                    }
                }
            }

            int exitCode = proc.waitFor();
            if (exitCode == 0) {
                classpath = lines.stream()
                        .map(File::new)
                        .filter(File::exists)
                        .collect(Collectors.toList());
            } else {
                Logger.getInstance().warn(GradleProject.class,
                        "Gradle printClasspath exited with code " + exitCode);
            }
        } catch (Exception e) {
            Logger.getInstance().warn(GradleProject.class, "Failed to resolve Gradle classpath: " + e.getMessage());
        }
    }
}
