package com.github.an0nn30.jpad.launchers.java;

import com.github.an0nn30.jpad.event.EventBus;
import com.github.an0nn30.jpad.event.EventType;
import com.github.an0nn30.jpad.launchers.BaseTerminalLaunchConfiguration;
import com.github.an0nn30.jpad.ui.EditorFrame;
import javax.swing.JOptionPane;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * A built‑in run configuration for Java (Maven) projects.
 * This configuration runs "mvn compile", "mvn package", and then executes the generated jar.
 * It uses the common terminal logic provided by BaseTerminalLaunchConfiguration.
 */
public class JavaMavenDefaultConfiguration extends BaseTerminalLaunchConfiguration {

    private final String name = "Java (Maven)";

    public JavaMavenDefaultConfiguration(EditorFrame editorFrame) {
        super(editorFrame);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute() {
        EventBus.publish(EventType.PROCESS_RUNNING.name(), this.name, this);
        // 1. Retrieve the project root directory.
        File projectRoot = editorFrame.getDirectoryTree().getRootDirectory();

        if (projectRoot == null) {
            JOptionPane.showMessageDialog(editorFrame,
                    "Project root directory not found. Cannot locate pom.xml.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 2. Validate that pom.xml exists.
        File pomFile = new File(projectRoot, "pom.xml");
        if (!pomFile.exists()) {
            JOptionPane.showMessageDialog(editorFrame,
                    "pom.xml not found in the project root directory.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 3. Parse pom.xml to extract jar information.
        String jarPath;
        try {
            jarPath = extractJarPathFromPom(pomFile);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(editorFrame,
                    "Error parsing pom.xml: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 4. Build the command string.
        String commands = "cd " + projectRoot + "\n" + "mvn compile\nmvn package\njava -jar " + jarPath + "\n";
        String terminalTitle = "Run: " + getName();
        // 5. Run the commands in the terminal using the base class method.
        runInTerminal(terminalTitle, commands);
    }

    /**
     * Parses the given pom.xml to extract the artifactId and version,
     * then constructs the jar path (assumed to be in "target/artifactId-version.jar").
     *
     * @param pomFile the pom.xml file.
     * @return the constructed jar path.
     * @throws Exception if parsing fails or required elements are missing.
     */
    private String extractJarPathFromPom(File pomFile) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pomFile);
        doc.getDocumentElement().normalize();

        NodeList artifactIdNodes = doc.getElementsByTagName("artifactId");
        NodeList versionNodes = doc.getElementsByTagName("version");

        if (artifactIdNodes.getLength() == 0 || versionNodes.getLength() == 0) {
            throw new Exception("artifactId or version not found in pom.xml.");
        }

        String artifactId = artifactIdNodes.item(0).getTextContent().trim();
        String version = versionNodes.item(0).getTextContent().trim();
        return "target/" + artifactId + "-" + version + ".jar";
    }
}