package com.github.an0nn30.ui;

import com.github.an0nn30.settings.Settings;
import com.github.an0nn30.settings.SettingsDialog;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class EditorFrame extends JFrame {
    private final EditorTabManager tabManager;
    private final EditorStatusBar statusBar;
    private final Settings settings;

    public EditorFrame() {
        super("");

        this.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
        this.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);

        // Load settings from a JSON file.
        settings = Settings.loadSettings();

        // Set the Look and Feel.
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Create UI components.
        tabManager = new EditorTabManager(this);
        statusBar = new EditorStatusBar(this);
        EditorMenuBar menuBar = new EditorMenuBar(this);

        setJMenuBar(menuBar.getMenuBar());

        add(tabManager.getTabbedPane(), BorderLayout.CENTER);
        add(statusBar.getPanel(), BorderLayout.SOUTH);

        // Add an initial tab.
        tabManager.addNewTab("Untitled", null);
    }

    public EditorTabManager getTabManager() {
        return tabManager;
    }

    public EditorStatusBar getStatusBar() {
        return statusBar;
    }

    public Settings getSettings() {
        return settings;
    }

    public void openSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this, settings);
        dialog.setVisible(true);
        // Optionally, update UI elements (font, theme, etc.) after settings change.
    }
}
