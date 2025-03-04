package com.github.an0nn30.jpad.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom panel that contains three sub-panels (left, center, and right)
 * with FlowLayout. Components can be added to these panels based on position.
 */
public class Panel extends JPanel {

    /**
     * Enum representing the positions of the sub-panels.
     */
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

    /**
     * Constructs a Panel with the specified editor reference and default hgap and vgap.
     * Default gaps are hgap = 5 and vgap = 0.
     *
     * @param editor an optional reference to a parent editor, or null if not used.
     */
    public Panel(Object editor) {
        this(editor, 5, 0);
    }

    /**
     * Constructs a Panel with no editor reference and default hgap and vgap.
     */
    public Panel() {
        this(null);
    }

    /**
     * Constructs a Panel with the specified editor reference and custom horizontal and vertical gaps.
     * This constructor initializes the left, center, and right sub-panels with a FlowLayout using
     * the provided gap settings.
     *
     * @param editor an optional reference to a parent editor, or null if not used.
     * @param hgap   the horizontal gap between components in the FlowLayout.
     * @param vgap   the vertical gap between components in the FlowLayout.
     */
    public Panel(Object editor, int hgap, int vgap) {
        super(new BorderLayout());
        this.editor = editor;

        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, hgap, vgap));
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, hgap, vgap));

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        leftComponents = new HashMap<>();
        centerComponents = new HashMap<>();
        rightComponents = new HashMap<>();
    }

    /**
     * Returns this Panel instance.
     *
     * @return the Panel instance.
     */
    public JPanel getPanel() {
        return this;
    }

    /**
     * Returns the left sub-panel.
     *
     * @return the left JPanel.
     */
    public JPanel getLeftPanel() {
        return leftPanel;
    }

    /**
     * Returns the center sub-panel.
     *
     * @return the center JPanel.
     */
    public JPanel getCenterPanel() {
        return centerPanel;
    }

    /**
     * Returns the right sub-panel.
     *
     * @return the right JPanel.
     */
    public JPanel getRightPanel() {
        return rightPanel;
    }

    /**
     * Returns the map of components added to the left sub-panel.
     *
     * @return a map of component keys to JComponent instances in the left panel.
     */
    public Map<String, JComponent> getLeftPanelComponents() {
        return leftComponents;
    }

    /**
     * Returns the map of components added to the center sub-panel.
     *
     * @return a map of component keys to JComponent instances in the center panel.
     */
    public Map<String, JComponent> getCenterPanelComponents() {
        return centerComponents;
    }

    /**
     * Returns the map of components added to the right sub-panel.
     *
     * @return a map of component keys to JComponent instances in the right panel.
     */
    public Map<String, JComponent> getRightPanelComponents() {
        return rightComponents;
    }

    /**
     * Adds a component to the specified sub-panel at the given position, identified by a key.
     * After adding, the panel is revalidated and repainted.
     *
     * @param key       a unique key identifying the component.
     * @param component the JComponent to be added.
     * @param position  the position (LEFT, CENTER, or RIGHT) where the component should be added.
     * @return the added component.
     * @throws IllegalArgumentException if an invalid panel position is provided.
     */
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

    /**
     * Adds a component to the specified sub-panel at the given position.
     * A key is generated using the component's hash code.
     *
     * @param component the JComponent to be added.
     * @param position  the position (LEFT, CENTER, or RIGHT) where the component should be added.
     */
    public void addComponent(JComponent component, PanelPosition position) {
        addComponent(String.valueOf(component.hashCode()), component, position);
    }

    /**
     * Recursively sets the background color of this panel and all its subcomponents.
     *
     * @param bg the background color to set.
     */
    public void setBackgroundRecursively(Color bg) {
        setBackground(bg);
        updateChildrenBackground(this, bg);
    }

    /**
     * Recursively updates the background color for the specified container and all its child components.
     *
     * @param container the container whose components will be updated.
     * @param bg        the background color to set.
     */
    private void updateChildrenBackground(Container container, Color bg) {
        for (Component comp : container.getComponents()) {
            comp.setBackground(bg);
            if (comp instanceof Container) {
                updateChildrenBackground((Container) comp, bg);
            }
        }
    }

    /**
     * Sets a vertical offset for the panel by adding an empty border on the top.
     *
     * @param offset the number of pixels to offset the panel from the top.
     */
    public void setVerticalOffset(int offset) {
        // Preserve any existing borders if needed; here we simply set an empty border.
        setBorder(BorderFactory.createEmptyBorder(offset, 0, 0, 0));
    }

    /**
     * Recursively makes the panel and all its child components transparent by setting them as non-opaque.
     */
    public void makeChildrenTransparentRecursively() {
        setOpaque(false);
        updateChildrenTransparency(this);
    }

    /**
     * Helper method that recursively sets each child component of the given container as non-opaque.
     *
     * @param container the container whose child components will be made transparent.
     */
    private void updateChildrenTransparency(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                ((JComponent) comp).setOpaque(false);
            }
            if (comp instanceof Container) {
                updateChildrenTransparency((Container) comp);
            }
        }
    }
}