package com.github.an0nn30.ui;
import com.formdev.flatlaf.IntelliJTheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditorStatusBar {
    public enum PanelPosition {
        LEFT, CENTER, RIGHT;
    }

    private final JPanel mainPanel;
    private final JPanel leftPanel;
    private final JPanel centerPanel;
    private final JPanel rightPanel;

    private final EditorFrame editor;

    // Maps to hold components for each panel.
    private final Map<String, JComponent> leftComponents;
    private final Map<String, JComponent> centerComponents;
    private final Map<String, JComponent> rightComponents;

    public EditorStatusBar(EditorFrame editor) {
        mainPanel = new JPanel(new BorderLayout());
        IntelliJTheme.setup(this.getClass().getResourceAsStream("/DarkPurple.theme.json"));
        this.editor = editor;

        // Set up sub-panels with FlowLayout to nicely "stack" components.
        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        // Add sub-panels to the main panel.
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // Initialize the component maps.
        leftComponents = new HashMap<>();
        centerComponents = new HashMap<>();
        rightComponents = new HashMap<>();

        // Example: Add a file type label to the right panel.
        addComponent("fileTypeComboBox", createFileTypeComboBox(), PanelPosition.RIGHT);
    }

    private JComboBox createFileTypeComboBox() {
        JComboBox<String> fileType = new JComboBox<>();
        fileType.addItem("text");
        fileType.addItem("python");
        fileType.addItem("java");
        fileType.addItem("cpp");

        fileType.addActionListener(e -> {
            String selectedItem = (String) fileType.getSelectedItem();
            this.editor.getTabManager().getActiveTextArea().setSyntaxEditingStyle(selectedItem);
        });
        return fileType;
    }

    // Returns the main panel for display.
    public JPanel getPanel() {
        return mainPanel;
    }

    // Getters for the component maps.
    public Map<String, JComponent> getLeftPanelComponents() {
        return leftComponents;
    }

    public Map<String, JComponent> getCenterPanelComponents() {
        return centerComponents;
    }

    public Map<String, JComponent> getRightPanelComponents() {
        return rightComponents;
    }

    /**
     * Adds a component to the specified panel and registers it with the provided key.
     *
     * @param key       the key to reference the component later.
     * @param component the Swing component to add.
     * @param position  the target panel position (LEFT, CENTER, or RIGHT).
     */
    public void addComponent(String key, JComponent component, PanelPosition position) {
        switch (position) {
            case LEFT:
                leftPanel.add(component);
                leftComponents.put(key, component);
                break;
            case CENTER:
                centerPanel.add(component);
                centerComponents.put(key, component);
                break;
            case RIGHT:
                rightPanel.add(component);
                rightComponents.put(key, component);
                break;
            default:
                throw new IllegalArgumentException("Invalid panel position: " + position);
        }
        // Refresh the main panel.
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * Convenience overload if you do not need to provide an explicit key.
     * A unique key is generated based on the component's hash code.
     *
     * @param component the Swing component to add.
     * @param position  the target panel position.
     */
    public void addComponent(JComponent component, PanelPosition position) {
        addComponent(String.valueOf(component.hashCode()), component, position);
    }

    /**
     * Convenience method to update the file type label stored in the right panel.
     *
     * @param text the new text for the file type label.
     */
    public void setText(String text) {
        JComponent comp = rightComponents.get("fileTypeLabel");
        if (comp instanceof JLabel) {
            ((JLabel) comp).setText(text);
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

//    /**
//     * Updates the Vim mode display on the status bar.
//     *
//     * @param mode The current mode, e.g., "NORMAL", "VISUAL", or "INSERT".
//     *             The label text is updated and the background is set to a corresponding pastel color.
//     */
//    public void setVimMode(String mode) {
//        // Update the label text.
//        vimModeLabel.setText(" " + mode + " ");
//
//        // Update the background color based on the mode.
//        switch (mode.toUpperCase()) {
//            case "NORMAL":
//                // Pastel green
//                vimModeLabel.setBackground(new Color(144, 238, 144));
//                break;
//            case "VISUAL":
//                // Light pastel blue
//                vimModeLabel.setBackground(new Color(173, 216, 230));
//                break;
//            case "INSERT":
//                // Light pastel yellow
//                vimModeLabel.setBackground(new Color(255, 255, 224));
//                break;
//            default:
//                // Use the panel's background color if unknown.
//                vimModeLabel.setBackground(panel.getBackground());
//                break;
//        }
//    }
}