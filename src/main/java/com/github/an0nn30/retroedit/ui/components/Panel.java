package com.github.an0nn30.retroedit.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Panel extends JPanel {

    public enum PanelPosition {
        LEFT, CENTER, RIGHT;
    }

    private final JPanel leftPanel;
    private final JPanel centerPanel;
    private final JPanel rightPanel;

    private final Map<String, JComponent> leftComponents;
    private final Map<String, JComponent> centerComponents;
    private final Map<String, JComponent> rightComponents;

    // An optional reference (if needed) to a parent Editor.
    private Object editor;

    public Panel(Object editor) {
        super(new BorderLayout());
        this.editor = editor;

        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        leftComponents = new HashMap<>();
        centerComponents = new HashMap<>();
        rightComponents = new HashMap<>();
    }

    public Panel() {
        this(null);
    }

    public JPanel getPanel() {
        return this;
    }

    public JPanel getLeftPanel() {
        return leftPanel;
    }

    public JPanel getCenterPanel() {
        return centerPanel;
    }

    public JPanel getRightPanel() {
        return rightPanel;
    }

    public Map<String, JComponent> getLeftPanelComponents() {
        return leftComponents;
    }

    public Map<String, JComponent> getCenterPanelComponents() {
        return centerComponents;
    }

    public Map<String, JComponent> getRightPanelComponents() {
        return rightComponents;
    }

    public JComponent addComponent(String key, JComponent component, PanelPosition position) {
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
        revalidate();
        repaint();
        return component;
    }

    public void addComponent(JComponent component, PanelPosition position) {
        addComponent(String.valueOf(component.hashCode()), component, position);
    }
}
