package com.github.an0nn30.retroedit.ui;

import com.github.an0nn30.retroedit.ui.search.ProjectFileSearchDialog;
import com.github.an0nn30.retroedit.ui.utils.FileManagerUtil;
import com.github.an0nn30.retroedit.jforms.AboutDialog;
import com.github.an0nn30.retroedit.jforms.Settings;
import com.github.an0nn30.retroedit.ui.actions.FindDialogAction;
import com.github.an0nn30.retroedit.ui.actions.GoToLineAction;
import com.github.an0nn30.retroedit.ui.actions.ReplaceDialogAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * MenuBar constructs and manages the application's main menu bar.
 * It creates File, View, and Edit menus with associated menu items, actions, and keyboard shortcuts.
 */
public class MenuBar {

    private final JMenuBar menuBar;
    private final EditorFrame editorFrame;

    /**
     * Constructs a MenuBar for the given EditorFrame.
     *
     * @param editorFrame the main editor frame.
     */
    public MenuBar(EditorFrame editorFrame) {
        this.editorFrame = editorFrame;
        this.menuBar = new JMenuBar();
        createMenus();
    }

    /**
     * Creates and adds the File, View, and Edit menus to the menu bar.
     */
    private void createMenus() {
        // On Mac, if the theme is not "retro", use the screen menu bar.
        if (System.getProperty("os.name").contains("Mac") &&
                !com.github.an0nn30.retroedit.settings.Settings.getInstance().getInterfaceTheme().equalsIgnoreCase("retro")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        // Create individual menus.
        JMenu fileMenu = createFileMenu();
        JMenu viewMenu = createViewMenu();
        JMenu editMenu = createEditMenu();

        // Add menus to the menu bar.
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(editMenu);
    }

    /**
     * Creates the File menu with options for opening, saving, managing tabs, and showing dialogs.
     *
     * @return the File menu.
     */
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");

        // Open menu item: uses FileManagerUtil.
        JMenuItem openItem = createMenuItem("Open",
                KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                e -> FileManagerUtil.openFile(editorFrame));
        fileMenu.add(openItem);

        // Save menu item.
        JMenuItem saveItem = createMenuItem("Save",
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                e -> editorFrame.getTabManager().saveFile(false));
        fileMenu.add(saveItem);

        // New Tab menu item.
        JMenuItem newTabItem = createMenuItem("New Tab",
                KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                e -> editorFrame.getTabManager().addNewTab("Untitled", null));
        fileMenu.add(newTabItem);

        // Close Tab menu item with special handling.
        JMenuItem closeTabItem = createMenuItem("Close Tab",
                KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                e -> handleCloseTab());
        fileMenu.add(closeTabItem);

        // About menu item.
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> new AboutDialog(editorFrame).setVisible(true));
        fileMenu.add(aboutItem);

        // Settings menu item.
        JMenuItem settingsItem = createMenuItem("Settings",
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                e -> {
                    Settings dialog = new Settings(editorFrame);
                    dialog.pack();
                    dialog.setVisible(true);
                });
        fileMenu.add(settingsItem);

        return fileMenu;
    }

    /**
     * Handles closing the current tab with specific rules:
     * - If more than one tab exists, simply close the current tab.
     * - If only one tab exists and it's "Untitled" (or modified untitled), show a warning.
     * - Otherwise, close the tab and create a new untitled tab.
     */
    private void handleCloseTab() {
        TabManager tabbedPane = editorFrame.getTabManager();
        int tabCount = tabbedPane.getTabCount();
        if (tabCount > 1) {
            tabbedPane.closeCurrentTab();
        } else if (tabCount == 1) {
            String tabTitle = tabbedPane.getTitleAt(0);
            if ("Untitled".equals(tabTitle) || tabTitle.startsWith("*Untitled")) {
                JOptionPane.showMessageDialog(editorFrame,
                        "Cannot close the only untitled tab.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                tabbedPane.closeCurrentTab();
                tabbedPane.addNewTab("Untitled", null);
            }
        }
    }

    /**
     * Creates the View menu with options to toggle various UI elements and adjust font sizes.
     *
     * @return the View menu.
     */
    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("View");

        // Toggle Terminal menu item.
        JMenuItem toggleTerminal = createMenuItem("Toggle Terminal",
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK),
                e -> {
                    // Uncomment and implement terminal toggle if needed.
                    // editorFrame.getSplitPane().toggleTerminal();
                });
        viewMenu.add(toggleTerminal);

        // Toggle Project View menu item.
        JMenuItem toggleProjectView = createMenuItem("Toggle Project View",
                KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                e -> editorFrame.toggleProjectView());
        viewMenu.add(toggleProjectView);

        // Increase Font Size menu item.
        JMenuItem increaseFontSize = createMenuItem("Increase Font Size",
                KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                e -> editorFrame.getTabManager().adjustFontSize(2));
        viewMenu.add(increaseFontSize);

        // Decrease Font Size menu item.
        JMenuItem decreaseFontSize = createMenuItem("Decrease Font Size",
                KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                e -> editorFrame.getTabManager().adjustFontSize(-2));
        viewMenu.add(decreaseFontSize);

        // Previous Tab menu item.
        JMenuItem prevTab = createMenuItem("Previous Tab",
                KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET,
                        InputEvent.SHIFT_DOWN_MASK | InputEvent.META_DOWN_MASK),
                e -> editorFrame.getTabManager().previousTab());
        viewMenu.add(prevTab);

        // Next Tab menu item.
        JMenuItem nextTab = createMenuItem("Next Tab",
                KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET,
                        InputEvent.SHIFT_DOWN_MASK | InputEvent.META_DOWN_MASK),
                e -> editorFrame.getTabManager().nextTab());
        viewMenu.add(nextTab);

        return viewMenu;
    }

    /**
     * Creates the Edit menu with actions for Find, Replace, and Go To Line.
     *
     * @return the Edit menu.
     */
    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");

        // Add Find, Replace, and Go To Line actions wrapped in JMenuItems.
        editMenu.add(new JMenuItem(new FindDialogAction(editorFrame)));
        editMenu.add(new JMenuItem(new ReplaceDialogAction(editorFrame)));
        editMenu.add(new JMenuItem(new GoToLineAction(editorFrame)));
        JMenuItem searchProjectItem = createMenuItem("Search in project...",
                KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK),
                e -> new ProjectFileSearchDialog(editorFrame).setVisible(true));
        editMenu.add(searchProjectItem);

        return editMenu;
    }

    /**
     * Creates a JMenuItem with the specified text, accelerator, and action listener.
     *
     * @param text        the text to display on the menu item.
     * @param accelerator the keyboard accelerator (may be null).
     * @param action      the action listener to handle events.
     * @return the constructed JMenuItem.
     */
    private JMenuItem createMenuItem(String text, KeyStroke accelerator, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        item.addActionListener(action);
        return item;
    }

    /**
     * Returns the JMenuBar created by this MenuBar instance.
     *
     * @return the JMenuBar.
     */
    public JMenuBar getMenuBar() {
        return menuBar;
    }
}