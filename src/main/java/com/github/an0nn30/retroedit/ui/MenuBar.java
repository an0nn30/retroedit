package com.github.an0nn30.retroedit.ui;

import com.github.an0nn30.retroedit.FileManagerUtil;
import com.github.an0nn30.retroedit.jforms.AboutDialog;
import com.github.an0nn30.retroedit.jforms.Settings;
import com.github.an0nn30.retroedit.ui.actions.FindDialogAction;
import com.github.an0nn30.retroedit.ui.actions.GoToLineAction;
import com.github.an0nn30.retroedit.ui.actions.ReplaceDialogAction;


import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class MenuBar {

    private final JMenuBar menuBar;
    private final EditorFrame editorFrame;

    public MenuBar(EditorFrame editorFrame) {
        this.editorFrame = editorFrame;
        menuBar = new JMenuBar();
        createMenus();
    }

    private void createMenus() {

        if (System.getProperty("os.name").contains("Mac") && !com.github.an0nn30.retroedit.settings.Settings.getInstance().getInterfaceTheme().equalsIgnoreCase("retro")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        JMenu fileMenu = new JMenu("File");
        JMenu viewMenu = new JMenu("View");
        JMenu editMenu = new JMenu("Edit");

        // Open
        JMenuItem openItem = new JMenuItem("Open");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        openItem.addActionListener(e -> FileManagerUtil.openFile(editorFrame));
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
        closeTabItem.addActionListener(e -> {
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
        });
        fileMenu.add(closeTabItem);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> new AboutDialog(editorFrame).setVisible(true));
        fileMenu.add(aboutItem);

        JMenuItem settingsTabItem = new JMenuItem("Settings");
        settingsTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        settingsTabItem.addActionListener(e -> {
            Settings dialog = new Settings(editorFrame);
            dialog.pack();
            dialog.setVisible(true);
        });
        fileMenu.add(settingsTabItem);

        JMenuItem toggleTerminal = new JMenuItem("Toggle Terminal");
//        toggleTerminal.addActionListener(e -> editor.getSplitPane().toggleTerminal());
        toggleTerminal.setAccelerator(
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_COMMA,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK
                )
        );
        viewMenu.add(toggleTerminal);

        JMenuItem toggleProjectView = new JMenuItem("Toggle Project View");
        toggleProjectView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        toggleProjectView.addActionListener(e -> editorFrame.toggleProjectView());
        viewMenu.add(toggleProjectView);

        // Increase Font Size
        JMenuItem increaseFontSize = new JMenuItem("Increase Font Size");
        increaseFontSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        increaseFontSize.addActionListener(e -> editorFrame.getTabManager().adjustFontSize(2));
        viewMenu.add(increaseFontSize);

        // Decrease Font Size
        JMenuItem decreaseFontSize = new JMenuItem("Decrease Font Size");
        decreaseFontSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        decreaseFontSize.addActionListener(e -> editorFrame.getTabManager().adjustFontSize(-2));
        viewMenu.add(decreaseFontSize);

        JMenuItem toggleStatusBar = new JMenuItem("Toggle Status Bar");
        toggleStatusBar.addActionListener(e -> editorFrame.getStatusPanel().toggle());
        viewMenu.add(toggleStatusBar);

        // Previous Tab
        JMenuItem prevTab = new JMenuItem("Previous Tab");
        prevTab.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_OPEN_BRACKET,
                InputEvent.SHIFT_DOWN_MASK | InputEvent.META_DOWN_MASK
        ));
        prevTab.addActionListener(e -> editorFrame.getTabManager().previousTab());
        viewMenu.add(prevTab);

        // Next Tab
        JMenuItem nextTab = new JMenuItem("Next Tab");
        nextTab.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_CLOSE_BRACKET,
                InputEvent.SHIFT_DOWN_MASK | InputEvent.META_DOWN_MASK
        ));
        nextTab.addActionListener(e -> editorFrame.getTabManager().nextTab());
        viewMenu.add(nextTab);

        editMenu.add(new JMenuItem(new FindDialogAction(editorFrame)));
        editMenu.add(new JMenuItem(new ReplaceDialogAction(editorFrame)));
        editMenu.add(new JMenuItem(new GoToLineAction(editorFrame)));



        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(editMenu);
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }
}
