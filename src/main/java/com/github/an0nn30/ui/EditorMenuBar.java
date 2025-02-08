package com.github.an0nn30.ui;


import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
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

        // Close tab
        JMenuItem closeTabItem = new JMenuItem("Close Tab");
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        closeTabItem.addActionListener(e -> {
            JTabbedPane tabbedPane = editorFrame.getTabManager().getTabbedPane();
            int tabCount = tabbedPane.getTabCount();

            if (tabCount > 1) {
                // More than one tab: simply close the current tab.
                editorFrame.getTabManager().closeCurrentTab();
            } else if (tabCount == 1) {
                // Only one tab is open. Check its title.
                String tabTitle = tabbedPane.getTitleAt(0);
                if ("Untitled".equals(tabTitle) || tabTitle.startsWith("*Untitled")) {
                    // Only an unsaved, untitled tab is open: don't allow closing it.
                    JOptionPane.showMessageDialog(editorFrame,
                            "Cannot close the only untitled tab.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    // A file is open in the only tab: close it and open a new untitled tab.
                    editorFrame.getTabManager().closeCurrentTab();
                    editorFrame.getTabManager().addNewTab("Untitled", null);
                }
            }
        });
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

        JMenuItem prevTab = new JMenuItem("Previous Tab");
        prevTab.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_OPEN_BRACKET,
                InputEvent.SHIFT_DOWN_MASK | InputEvent.META_DOWN_MASK
        ));
        prevTab.addActionListener(e -> {
            // Assuming you have access to your JTabbedPane
            JTabbedPane tabbedPane = editorFrame.getTabManager().getTabbedPane();
            int tabCount = tabbedPane.getTabCount();
            if (tabCount > 0) {
                int currentIndex = tabbedPane.getSelectedIndex();
                // Cycle to the previous tab (wrap around to the last tab)
                int previousIndex = (currentIndex - 1 + tabCount) % tabCount;
                tabbedPane.setSelectedIndex(previousIndex);
            }
        });
        viewMenu.add(prevTab);

// Next Tab: cmd+shift+] (VK_CLOSE_BRACKET)
        JMenuItem nextTab = new JMenuItem("Next Tab");
        nextTab.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_CLOSE_BRACKET,
                InputEvent.SHIFT_DOWN_MASK | InputEvent.META_DOWN_MASK
        ));
        nextTab.addActionListener(e -> {
            JTabbedPane tabbedPane = editorFrame.getTabManager().getTabbedPane();
            int tabCount = tabbedPane.getTabCount();
            if (tabCount > 0) {
                int currentIndex = tabbedPane.getSelectedIndex();
                // Cycle to the next tab (wrap around to the first tab)
                int nextIndex = (currentIndex + 1) % tabCount;
                tabbedPane.setSelectedIndex(nextIndex);
            }
        });
        viewMenu.add(nextTab);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }
}