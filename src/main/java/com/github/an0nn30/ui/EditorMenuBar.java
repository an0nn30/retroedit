package com.github.an0nn30.ui;


import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

public class EditorMenuBar {
    private final JMenuBar menuBar;
    private final EditorFrame editorFrame;

    public EditorMenuBar(EditorFrame editorFrame) {
        this.editorFrame = editorFrame;
        menuBar = new JMenuBar();
        createMenus();
    }

    private void createMenus() {
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        JMenu fileMenu = new JMenu("File");
        JMenu viewMenu = new JMenu("View");

        // Open
        JMenuItem openItem = new JMenuItem("Open");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        openItem.addActionListener(e -> editorFrame.getTabManager().openFile());
        fileMenu.add(openItem);

        // Save
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveItem.addActionListener(e -> editorFrame.getTabManager().saveFile(false));
        fileMenu.add(saveItem);

        // New Tab
        JMenuItem newTabItem = new JMenuItem("New Tab");
        newTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newTabItem.addActionListener(e -> editorFrame.getTabManager().addNewTab("Untitled", null));
        fileMenu.add(newTabItem);

        // Close Tab
        JMenuItem closeTabItem = new JMenuItem("Close Tab");
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        closeTabItem.addActionListener(e -> editorFrame.getTabManager().closeCurrentTab());
        fileMenu.add(closeTabItem);

        // Settings
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        settingsItem.addActionListener(e -> editorFrame.openSettingsDialog());
        fileMenu.add(settingsItem);

        // View: Increase Font Size
        JMenuItem increaseFontSize = new JMenuItem("Increase Font Size");
        increaseFontSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        increaseFontSize.addActionListener(e -> editorFrame.getTabManager().adjustFontSize(2));
        viewMenu.add(increaseFontSize);

        // View: Decrease Font Size
        JMenuItem decreaseFontSize = new JMenuItem("Decrease Font Size");
        decreaseFontSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        decreaseFontSize.addActionListener(e -> editorFrame.getTabManager().adjustFontSize(-2));
        viewMenu.add(decreaseFontSize);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }
}