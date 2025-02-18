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
import com.jediterm.terminal.ui.JediTermWidget;
import org.fife.ui.rsyntaxtextarea.Theme;
import javax.swing.*;
import java.awt.*;

public class Editor extends JFrame {

    private MainToolbar mainToolbar;
    private TabManager tabManager;
    private StatusPanel statusPanel;
    private JSplitPane projectEditorSplit;
    private JSplitPane editorTerminalSplit;
    private final MacUtils macUtils;
    private DirectoryTree directoryTree;

    // Use a single instance of the terminal widget.
    private JediTermWidget terminal;

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

        // Create the terminal widget only once.
        terminal = Terminal.createTerminalWidget(this);

        // Set up the menu bar.
        setJMenuBar(new MenuBar(this).getMenuBar());

        // Directory tree for the project.
        directoryTree = new DirectoryTree(".", this);
    }

    /**
     * Layout all components within the frame.
     */
    private void layoutComponents() {
        // Create the vertical split pane with the tab manager on top and the terminal on bottom.
        editorTerminalSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                tabManager,
                terminal
        );
        // With resize weight set to 1.0, the top component (tabManager) gets nearly all extra space.
        editorTerminalSplit.setResizeWeight(1.0);
        editorTerminalSplit.setOneTouchExpandable(true);
        editorTerminalSplit.setContinuousLayout(true);

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
                editorTerminalSplit
        );
        projectEditorSplit.setDividerLocation(200);
        projectEditorSplit.setResizeWeight(0.3);
        projectEditorSplit.setOneTouchExpandable(true);
        projectEditorSplit.setContinuousLayout(true);

        // Lay out the frame using BorderLayout.
        setLayout(new BorderLayout());
        add(mainToolbar, BorderLayout.NORTH);
        add(projectEditorSplit, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Initially hide the project view and terminal.
        hideProjectView();
        hideTerminal();
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
//            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//            setUndecorated(true);
//            getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

            if (theme.equalsIgnoreCase("light")) {
//                UIManager.setLookAndFeel(new FlatIntelliJLaf());
                Theme textAreaTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
                textAreaTheme.apply(textArea);
            } else if (theme.equalsIgnoreCase("dark")) {
//                UIManager.setLookAndFeel(new FlatDarculaLaf());

                Theme textAreaTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
                textAreaTheme.apply(textArea);
//            }
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

    /**
     * Toggles the visibility of the project view.
     */
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


    /**
     * Recursively applies dark mode styling to the given component and all its child components.
     *
     * @param comp The root component to start applying dark mode.
     */
    private void applyDarkMode(Component comp) {
        if (comp instanceof JComponent) {
            JComponent jComp = (JComponent) comp;
            // Set a dark background and a light foreground.
            jComp.setBackground(new Color(43, 43, 43));   // Dark background color.
            jComp.setForeground(new Color(187, 187, 187)); // Light text color.

            // Optional: Adjust borders, fonts, or other properties if needed.
            // For example:
            // if (jComp instanceof JButton) {
            //     jComp.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
            // }
        }

        // If the component is a container, recursively apply dark mode to its children.
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyDarkMode(child);
            }
        }
    }

    public DirectoryTree getDirectoryTree() {
        return directoryTree;
    }

    /**
     * Hides the project view by moving its divider completely.
     */
    public void hideProjectView() {
        projectEditorSplit.setDividerLocation(1);
    }

    /**
     * Hides the terminal by setting the divider location of the vertical split pane to 100%.
     * This gives the top component (the TabManager) all the space.
     */
    public void hideTerminal() {
        // Invoke later to ensure the split pane has been laid out.
        SwingUtilities.invokeLater(() -> {
            // Setting the divider location to 1.0 (i.e. 100% of the height) effectively hides the terminal.
            editorTerminalSplit.setDividerLocation(1.0);
        });
    }
}