package com.github.an0nn30.jpad.project;

import com.github.an0nn30.jpad.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MavenProject implements JavaProject {

    private final File projectRoot;
    private List<File> classpath = new ArrayList<>();
    private List<String> modules = new ArrayList<>();
    private String sourceLevel = "17";
    private boolean resolved = false;

    public MavenProject(File projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public File getProjectRoot() {
        return projectRoot;
    }

    @Override
    public String getBuildSystem() {
        return "maven";
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
        return new File(projectRoot, "target/classes");
    }

    @Override
    public String getSourceLevel() {
        return sourceLevel;
    }

    @Override
    public void resolveDependencies() throws Exception {
        parsePom();
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

    private void parsePom() {
        File pomFile = new File(projectRoot, "pom.xml");
        if (!pomFile.exists()) return;

        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var builder = factory.newDocumentBuilder();
            var doc = builder.parse(pomFile);
            doc.getDocumentElement().normalize();

            // Parse modules
            var moduleNodes = doc.getElementsByTagName("module");
            for (int i = 0; i < moduleNodes.getLength(); i++) {
                modules.add(moduleNodes.item(i).getTextContent().trim());
            }

            // Parse source level from properties
            var props = doc.getElementsByTagName("properties");
            if (props.getLength() > 0) {
                var propElement = props.item(0);
                var children = propElement.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    var node = children.item(i);
                    if ("maven.compiler.source".equals(node.getNodeName())) {
                        sourceLevel = node.getTextContent().trim();
                    }
                }
            }
        } catch (Exception e) {
            Logger.getInstance().warn(MavenProject.class, "Failed to parse pom.xml: " + e.getMessage());
        }
    }

    private void resolveClasspath() {
        try {
            File tmpFile = Files.createTempFile("mvn-cp-", ".txt").toFile();
            tmpFile.deleteOnExit();

            ProcessBuilder pb = new ProcessBuilder(
                    "mvn", "dependency:build-classpath",
                    "-DincludeScope=compile",
                    "-Dmdep.outputFile=" + tmpFile.getAbsolutePath(),
                    "-q"
            );
            pb.directory(projectRoot);
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            // Consume output to prevent blocking
            try (var reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                while (reader.readLine() != null) { /* drain */ }
            }

            int exitCode = proc.waitFor();
            if (exitCode == 0 && tmpFile.exists()) {
                String cpString = Files.readString(tmpFile.toPath()).trim();
                if (!cpString.isEmpty()) {
                    classpath = Arrays.stream(cpString.split(File.pathSeparator))
                            .map(File::new)
                            .filter(File::exists)
                            .collect(java.util.stream.Collectors.toList());
                }
            } else {
                Logger.getInstance().warn(MavenProject.class,
                        "mvn dependency:build-classpath exited with code " + exitCode);
            }
        } catch (Exception e) {
            Logger.getInstance().warn(MavenProject.class, "Failed to resolve Maven classpath: " + e.getMessage());
        }
    }
}
