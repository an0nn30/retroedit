package com.github.an0nn30.retroedit.ui;

import com.formdev.flatlaf.*;
import com.github.an0nn30.retroedit.event.EventRecord;
import com.github.an0nn30.retroedit.event.EventBus;
import com.github.an0nn30.retroedit.event.EventType;
import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.components.DirectoryTree;
import com.github.an0nn30.retroedit.ui.components.Terminal;
import com.github.an0nn30.retroedit.ui.components.TextArea;
import com.github.an0nn30.retroedit.ui.platform.MacUtils;
import org.fife.ui.rsyntaxtextarea.Theme;
import javax.swing.*;
import java.awt.*;

public class Editor extends JFrame {

    private MainToolbar mainToolbar;
    private TabManager tabManager;
    private StatusPanel statusPanel;
    private SplitPane splitPane;
    private JSplitPane projectEditorSplit;
    private final MacUtils macUtils;
    private DirectoryTree directoryTree;

    private boolean isProjectViewToggled = false;

    public Editor() {
        super("Retro Edit");

        macUtils = new MacUtils();
        setupTheme();
        initializeFrame();
        initializeComponents();
        layoutComponents();
        registerEventSubscriptions();
        startEditor();
    }

    /**
     * Basic frame setup.
     */
    private void initializeFrame() {
        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Initialize all the components used in the Editor.
     */
    private void initializeComponents() {
        // Top-level components
        mainToolbar = new MainToolbar(this);
        tabManager = new TabManager(this);
        statusPanel = new StatusPanel();

        // Set up the menu bar.
        setJMenuBar(new MenuBar(this).getMenuBar());

        // Main split pane with editor and terminal.
        splitPane = new SplitPane(tabManager, Terminal.createTerminalWidget(this));
        splitPane.setResizeWeight(1.0);

        // Directory tree for the project.
        directoryTree = new DirectoryTree(".", this);

    }

    /**
     * Layout all components within the frame.
     */
    private void layoutComponents() {
        // Wrap the directory tree in a scroll pane.
        JScrollPane treeScrollPane = new JScrollPane(
                directoryTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        // Split pane dividing the project tree and the main editor/terminal pane.
        projectEditorSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                treeScrollPane,
                splitPane
        );
        projectEditorSplit.setDividerLocation(200);
        projectEditorSplit.setResizeWeight(0.3);
        projectEditorSplit.setOneTouchExpandable(true);
        projectEditorSplit.setContinuousLayout(true);

        // Lay out the frame using BorderLayout.
        setLayout(new BorderLayout());
        add(mainToolbar, BorderLayout.NORTH);
        add(projectEditorSplit, BorderLayout.CENTER);
//        hideProjectView();
        add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * Subscribe to events that affect the UI.
     */
    private void registerEventSubscriptions() {
        EventBus.subscribe(EventType.TAB_UPDATED.name(), (EventRecord<Object> eventRecord) -> {
            setTitle(eventRecord.data().toString());
        });
        EventBus.subscribe(EventType.THEME_CHANGED.name(), this::updateInterfaceTheme);
    }

    /**
     * Perform any startup actions.
     */
    private void startEditor() {
        // Open one untitled tab to begin.
        tabManager.addNewTab("Untitled", new TextArea(this));
        updateInterfaceTheme(null);
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    /**
     * Updates the UI theme based on the provided event record or saved settings.
     */
    private void updateInterfaceTheme(EventRecord<Object> eventRecord) {
        String theme = eventRecord == null ? Settings.getInstance().getInterfaceTheme() : eventRecord.data().toString();
        var textArea = tabManager.getActiveTextArea();

        try {
            if (theme.equalsIgnoreCase("light")) {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
                Theme textAreaTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
                textAreaTheme.apply(textArea);
            } else if (theme.equalsIgnoreCase("dark")) {
                UIManager.setLookAndFeel(new FlatDarculaLaf());
                Theme textAreaTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
                textAreaTheme.apply(textArea);
            } else if (theme.equalsIgnoreCase("retro")) {
                setUndecorated(true);
                getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                Theme textAreaTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
                textAreaTheme.apply(textArea);
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        } catch (Exception e) {
            Logger.getInstance().error(StatusPanel.class, "Failed to set theme: " + e.getMessage());
            return;
        }

        // Update the UI for all components.
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }

    /**
     * Set up the initial window theme.
     */
    private void setupTheme() {
        if (Settings.getSettings().isEnableClassicTheme()) {
            setUndecorated(true);
            getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        } else {
            FlatIntelliJLaf.setup();
        }
    }

    public void toggleProjectView() {
        isProjectViewToggled = !isProjectViewToggled;
        Logger.getInstance().info(getClass(), "Project view toggled: " + isProjectViewToggled);
        if (isProjectViewToggled) {
            projectEditorSplit.setDividerLocation(200);
            this.directoryTree.requestFocus();

        } else {
            projectEditorSplit.setDividerLocation(1);
            this.tabManager.requestFocus();
        }
    }


    public SplitPane getSplitPane() {
        return splitPane;
    }

    public DirectoryTree getDirectoryTree() {
        return directoryTree;
    }
    public JSplitPane getProjectEditorSplit() {
        return projectEditorSplit;
    }
}