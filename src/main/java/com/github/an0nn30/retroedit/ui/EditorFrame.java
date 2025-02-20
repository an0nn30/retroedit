// File: com/github/an0nn30/retroedit/ui/Editor.java
package com.github.an0nn30.retroedit.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.an0nn30.retroedit.event.EventBus;
import com.github.an0nn30.retroedit.event.EventRecord;
import com.github.an0nn30.retroedit.event.EventType;
import com.github.an0nn30.retroedit.ui.components.DirectoryTree;
import com.github.an0nn30.retroedit.ui.components.Terminal;
import com.github.an0nn30.retroedit.ui.components.TextArea;
import com.github.an0nn30.retroedit.ui.search.SearchController;
import com.github.an0nn30.retroedit.ui.theme.ThemeManager;
import com.jediterm.terminal.ui.JediTermWidget;
import javax.swing.*;
import java.awt.*;

public class EditorFrame extends JFrame {

    private boolean terminalShowing = true;
    private MainToolbar mainToolbar;
    private TabManager tabManager;
    private StatusPanel statusPanel;
    private JSplitPane projectEditorSplit;
    private JSplitPane editorTerminalSplit;
    private DirectoryTree directoryTree;
    private JediTermWidget terminal; // Your terminal widget instance
    private boolean isProjectViewToggled = false;
    private SearchController searchController;

    public EditorFrame() {
        super("Retro Edit");
        ThemeManager.setupWindowFrame(this); // Delegate theme setup

        initializeFrame();
        initializeComponents();
        layoutComponents();
        registerEventSubscriptions();
        startEditor();
        disableAllToolbars(this);
    }

    private void initializeFrame() {
        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initializeComponents() {
        mainToolbar = new MainToolbar(this);
        tabManager = new TabManager(this);
        statusPanel = new StatusPanel(this);
        terminal = Terminal.createTerminalWidget(this);
        setJMenuBar(new MenuBar(this).getMenuBar());
        directoryTree = new DirectoryTree(this);
        searchController = new SearchController(this); // delegate for search dialogs
    }

    private void layoutComponents() {
        editorTerminalSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                tabManager,
                terminal
        );
        editorTerminalSplit.setResizeWeight(1.0);
        editorTerminalSplit.setOneTouchExpandable(true);
        editorTerminalSplit.setContinuousLayout(true);

        JScrollPane treeScrollPane = new JScrollPane(
                directoryTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        projectEditorSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                treeScrollPane,
                editorTerminalSplit
        );
        projectEditorSplit.setDividerLocation(200);
        projectEditorSplit.setResizeWeight(0.3);
        projectEditorSplit.setOneTouchExpandable(true);
        projectEditorSplit.setContinuousLayout(true);

        setLayout(new BorderLayout());
        add(mainToolbar, BorderLayout.NORTH);
        add(projectEditorSplit, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        hideProjectView();
        hideTerminal();
    }

    private void registerEventSubscriptions() {
        EventBus.subscribe(EventType.TAB_UPDATED.name(), (EventRecord<Object> eventRecord) -> {
            setTitle(eventRecord.data().toString());
        });
        EventBus.subscribe(EventType.THEME_CHANGED.name(),
                eventRecord -> ThemeManager.updateInterfaceTheme(this, eventRecord));
    }

    private void startEditor() {
        // Open one untitled tab to begin.
        tabManager.addNewTab("Untitled", new TextArea(this));
        ThemeManager.updateInterfaceTheme(this, null);
        toggleTerminal();

    }

    // Accessors for other components used by actions or the search controller
    public TabManager getTabManager() {
        return tabManager;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public DirectoryTree getDirectoryTree() {
        return directoryTree;
    }

    public SearchController getSearchController() {
        return searchController;
    }

    // UI methods for toggling/hiding panels.
    public void toggleProjectView() {
        isProjectViewToggled = !isProjectViewToggled;
        if (isProjectViewToggled) {
            projectEditorSplit.setDividerLocation(200);
            directoryTree.requestFocus();
        } else {
            projectEditorSplit.setDividerLocation(1);
            tabManager.requestFocus();
        }
    }

    public void toggleTerminal() {
        if (terminalShowing) {
            
            SwingUtilities.invokeLater(() -> editorTerminalSplit.setDividerLocation(1.0));
            terminalShowing = false;
        } else {
            SwingUtilities.invokeLater(() -> editorTerminalSplit.setDividerLocation(0.75));
            terminalShowing = true;
        }
    }

    public void hideProjectView() {
        projectEditorSplit.setDividerLocation(1);
    }

    public void hideTerminal() {
        SwingUtilities.invokeLater(() -> editorTerminalSplit.setDividerLocation(1.0));
    }

    private void disableFloatingToolbars(Component component) {
        if (component instanceof JToolBar) {
            ((JToolBar) component).setFloatable(false);
        } else if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                disableFloatingToolbars(child); // Recursively check child components
            }
        }
    }

    private void disableAllToolbars(JFrame frame) {
        FlatLightLaf.setup();
        disableFloatingToolbars(frame.getContentPane()); // Start traversal from the frame's content pane
    }
}
