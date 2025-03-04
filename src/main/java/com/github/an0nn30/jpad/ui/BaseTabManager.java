package com.github.an0nn30.jpad.ui;


import javax.swing.*;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public abstract class BaseTabManager<T extends Component> extends JTabbedPane {

    protected T lastFocusedComponent;

    public BaseTabManager(int tabPlacement) {
        super(tabPlacement);
    }

    /**
     * Utility to add a new tab with a focus listener that updates the last-focused component.
     */
    protected void addComponentTab(String title, T component) {
        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                lastFocusedComponent = component;
            }
        });
        addTab(title, component);
    }

    /**
     * Returns the active component for the tab manager.
     * Subclasses may override this if their components are wrapped (e.g., in a JScrollPane).
     */
    public T getActiveComponent() {
        Component comp = getSelectedComponent();
        if (comp != null) {
            return (T) comp;
        }
        return lastFocusedComponent;
    }
}
