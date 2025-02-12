package com.github.an0nn30.ui;

import com.formdev.flatlaf.IntelliJTheme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BasePanel extends JPanel {

    public enum PanelPosition {
        LEFT, CENTER, RIGHT;
    }

    // Main container and its sub-panels for each region.
    private final JPanel leftPanel;
    private final JPanel centerPanel;
    private final JPanel rightPanel;

    // Maps to hold components for each panel.
    private final Map<String, JComponent> leftComponents;
    private final Map<String, JComponent> centerComponents;
    private final Map<String, JComponent> rightComponents;

    private final EditorFrame editor;

    public BasePanel(EditorFrame editor) {
        super(new BorderLayout());
        IntelliJTheme.setup(this.getClass().getResourceAsStream("/DarkPurple.theme.json"));
        this.editor = editor;

        // Set up sub-panels with FlowLayout to nicely "stack" components.
        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        // Add sub-panels to the main panel.
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Initialize the component maps.
        leftComponents = new HashMap<>();
        centerComponents = new HashMap<>();
        rightComponents = new HashMap<>();

        // Example: Add a file type label to the right panel.


    }

    // Returns the main panel for display.
    public JPanel getPanel() {
        return this;
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
    public void addComponent(String key, JComponent component, EditorStatusBar.PanelPosition position) {
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
        revalidate();
        repaint();
    }

    /**
     * Convenience overload if you do not need to provide an explicit key.
     * A unique key is generated based on the component's hash code.
     *
     * @param component the Swing component to add.
     * @param position  the target panel position.
     */
    public void addComponent(JComponent component, EditorStatusBar.PanelPosition position) {
        addComponent(String.valueOf(component.hashCode()), component, position);
    }
}
