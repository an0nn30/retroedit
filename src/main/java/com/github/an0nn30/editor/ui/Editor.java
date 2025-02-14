package com.github.an0nn30.editor.ui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.github.an0nn30.editor.event.Event;
import com.github.an0nn30.editor.event.EventBus;
import com.github.an0nn30.editor.event.EventType;
import com.github.an0nn30.editor.ui.components.TextArea;

import javax.swing.*;
import java.awt.*;

public class Editor extends JFrame {

    private MainToolbar mainToolbar;
    private TabManager tabManager;
    private StatusPanel statusPanel;

    public Editor() {
        super("Editor");
        FlatIntelliJLaf.install();
        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Initialize settings (could load from a file or user preferences)
        Settings.initialize();

        // Create top-level components with this Editor as reference.
        mainToolbar = new MainToolbar(this);
        tabManager = new TabManager(this);
        statusPanel = new StatusPanel();

        setJMenuBar(new MenuBar(this).getMenuBar());

        // Layout
        add(mainToolbar, BorderLayout.NORTH);
        add(tabManager, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Start with one untitled tab.
        tabManager.addNewTab("Untitled", new TextArea(this));

        // Subscribe to tab title update events (non‑input‑critical).
        EventBus.subscribe(EventType.TAB_UPDATED.name(), (Event<Object> event) -> {
            String title = event.data().toString();
            setTitle(title);
        });
    }

    public MainToolbar getMainToolbar() {
        return mainToolbar;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Editor editor = new Editor();
            editor.setVisible(true);
        });
    }
}
