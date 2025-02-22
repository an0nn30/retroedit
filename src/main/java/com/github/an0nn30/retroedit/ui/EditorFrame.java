package com.github.an0nn30.retroedit.ui;

import com.github.an0nn30.retroedit.event.EventBus;
import com.github.an0nn30.retroedit.event.EventType;
import com.github.an0nn30.retroedit.ui.components.DirectoryTree;
import com.github.an0nn30.retroedit.ui.components.Terminal;
import com.github.an0nn30.retroedit.ui.components.TextArea;
import com.github.an0nn30.retroedit.ui.search.SearchController;
import com.github.an0nn30.retroedit.ui.theme.ThemeManager;
import com.jediterm.terminal.ui.JediTermWidget;
import org.fife.rsta.ac.AbstractSourceTree;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rsta.ac.java.tree.JavaOutlineTree;
import org.fife.rsta.ac.js.tree.JavaScriptOutlineTree;
import org.fife.rsta.ac.xml.tree.XmlOutlineTree;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_JAVA;

/**
 * The main editor frame for the Retro Edit application.
 */
public class EditorFrame extends JFrame {

    private boolean terminalShowing = true;
    private MainToolbar mainToolbar;
    private TabManager tabManager;
    private StatusPanel statusPanel;
    private JSplitPane projectEditorSplit;
    private JSplitPane editorTerminalSplit;
    private DirectoryTree directoryTree;
    private JediTermWidget terminal;
    private boolean isProjectViewToggled = false;
    private boolean isTerminalToggled = false;
    private SearchController searchController;
    private int projectViewDividerLocation = 200;
    private int terminalViewDividerLocation = 150; // default initial value
    private AbstractSourceTree sourceTree;
    private JScrollPane treeSP;
    LanguageSupportFactory lsf;

    /**
     * Constructs the EditorFrame and initializes the UI.
     */
    public EditorFrame() {
        super("Retro Edit");

        ThemeManager.setupWindowFrame(this);
        SwingUtilities.invokeLater(() -> {
            initializeFrame();
            initializeComponents();
            layoutComponents();
            registerEventSubscriptions();
            startEditor();
            disableAllToolbars(this);
        });

    }

    /**
     * Initializes frame properties such as size and close operation.
     */
    private void initializeFrame() {
        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Initializes UI components used in the frame.
     */
    private void initializeComponents() {
        mainToolbar = new MainToolbar(this);
        tabManager = new TabManager(this);
        statusPanel = new StatusPanel(this);
        setJMenuBar(new MenuBar(this).getMenuBar());
        JTree dummy = new JTree((TreeNode)null);
        treeSP = new JScrollPane(dummy);
        directoryTree = new DirectoryTree(this);
        searchController = null; // Lazy-load search controller
        lsf = LanguageSupportFactory.get();

        loadTerminalWidget();
    }

    /**
     * Asynchronously loads the terminal widget.
     */
    private void loadTerminalWidget() {
        SwingWorker<JediTermWidget, Void> terminalLoader = new SwingWorker<>() {
            @Override
            protected JediTermWidget doInBackground() {
                return Terminal.createTerminalWidget(EditorFrame.this);
            }

            @Override
            protected void done() {
                try {
                    terminal = get();
                    editorTerminalSplit.setBottomComponent(terminal);
                } catch (Exception ignored) {
                    // Exception handling can be added here if needed.
                }
            }
        };
        terminalLoader.execute();
    }

    /**
     * Lays out components within the frame.
     */
    private void layoutComponents() {
        editorTerminalSplit = createEditorTerminalSplit();
        addFixedDividerListenersToTerminal(editorTerminalSplit);

        projectEditorSplit = createProjectEditorSplit();
        addFixedDividerListenersToProject(projectEditorSplit);


        addComponentsToFrame();

        // Defer hiding views to prevent blocking UI
        SwingUtilities.invokeLater(() -> {
            hideProjectView();
            hideTerminal();
        });
    }

    /**
     * Creates the split pane for the editor and terminal.
     *
     * @return a configured JSplitPane for the editor and terminal.
     */
    private JSplitPane createEditorTerminalSplit() {
        // Remove automatic resizing by setting resize weight to 0.
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabManager, new JPanel());
        split.setResizeWeight(0);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        return split;
    }

    /**
     * Adds listeners to the terminal split pane to keep the divider fixed when toggled.
     *
     * @param split the JSplitPane representing the editor-terminal split.
     */
    private void addFixedDividerListenersToTerminal(JSplitPane split) {
        // Update stored divider when the user moves it while terminal is expanded.
        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            if (isTerminalToggled) {
                int newLocation = (Integer) evt.getNewValue();
                if (newLocation > 1) {
                    terminalViewDividerLocation = newLocation;
                }
            }
        });

        // On frame resize, reapply the stored fixed divider location.
        split.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (isTerminalToggled) {
                    split.setDividerLocation(terminalViewDividerLocation);
                } else {
                    // Use a fixed minimal value when collapsed.
                    split.setDividerLocation(1);
                }
            }
        });
    }

    /**
     * Creates the split pane for the project directory and the editor-terminal split.
     *
     * @return a configured JSplitPane for the project view and editor-terminal view.
     */
    private JSplitPane createProjectEditorSplit() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(directoryTree), treeSP);
        JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split, editorTerminalSplit);
        // Remove automatic resizing by setting resize weight to 0.
        split2.setResizeWeight(0);
        split2.setOneTouchExpandable(true);
        split2.setContinuousLayout(true);
        return split2;
    }

    /**
     * Adds listeners to the project-editor split pane to keep the divider fixed when toggled.
     *
     * @param split the JSplitPane representing the project-editor split.
     */
    private void addFixedDividerListenersToProject(JSplitPane split) {
        // Update stored divider when the user moves it while project view is expanded.
        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            if (isProjectViewToggled) {
                int newLocation = (Integer) evt.getNewValue();
                if (newLocation > 1) {
                    System.out.println("Project view divider location: " + newLocation + " (old: " + projectViewDividerLocation + ")");
                    projectViewDividerLocation = newLocation;
                }
            }
        });

        // On frame resize, reapply the stored fixed divider location.
        split.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (isProjectViewToggled) {
                    split.setDividerLocation(projectViewDividerLocation);
                } else {
                    split.setDividerLocation(1);
                }
            }
        });
    }

    /**
     * Adds components to the frame using a BorderLayout.
     */
    private void addComponentsToFrame() {
        setLayout(new BorderLayout());
        add(mainToolbar, BorderLayout.NORTH);
        add(projectEditorSplit, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * Registers event subscriptions for the frame.
     */
    private void registerEventSubscriptions() {
        SwingUtilities.invokeLater(() -> {
            EventBus.subscribe(EventType.TAB_UPDATED.name(), eventRecord -> {
                refreshSourceTree();
                setTitle(eventRecord.data().toString());
            });
            EventBus.subscribe(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(), eventRecord -> {
                refreshSourceTree();
            });
        });
    }

    /**
     * Starts the editor by adding an initial tab and applying theme settings.
     */
    private void startEditor() {
        SwingUtilities.invokeLater(() -> {
            tabManager.addNewTab("Untitled", new TextArea(this));
            toggleTerminalView();
        });
        ThemeManager.updateInterfaceTheme(this, null);
    }

    /**
     * Returns the search controller, initializing it if necessary.
     *
     * @return the SearchController instance.
     */
    public SearchController getSearchController() {
        if (searchController == null) {
            searchController = new SearchController(this);
        }
        return searchController;
    }

    /**
     * Returns the tab manager.
     *
     * @return the TabManager instance.
     */
    public TabManager getTabManager() {
        return tabManager;
    }

    /**
     * Returns the status panel.
     *
     * @return the StatusPanel instance.
     */
    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    /**
     * Returns the directory tree.
     *
     * @return the DirectoryTree instance.
     */
    public DirectoryTree getDirectoryTree() {
        return directoryTree;
    }

    /**
     * Toggles the visibility of the project view.
     */
    public void toggleProjectView() {
        isProjectViewToggled = !isProjectViewToggled;
        if (isProjectViewToggled) {
            // When expanding, reapply the stored divider location.
            projectEditorSplit.setDividerLocation(projectViewDividerLocation);
            directoryTree.requestFocus();
        } else {
            // When collapsing, set a fixed minimal divider location.
            projectEditorSplit.setDividerLocation(1);
            tabManager.getActiveTextArea().requestFocus();
        }
    }

    /**
     * Toggles the visibility of the terminal view.
     */
    public void toggleTerminalView() {
        isTerminalToggled = !isTerminalToggled;
        if (isTerminalToggled) {
            // When expanding, reapply the stored divider location.
            editorTerminalSplit.setDividerLocation(terminalViewDividerLocation);
            if (terminal != null) {
                terminal.requestFocus();
            }
        } else {
            // When collapsing, use a fixed minimal divider location.
            editorTerminalSplit.setDividerLocation(editorTerminalSplit.getMaximumDividerLocation());
            tabManager.requestFocus();
        }
        System.out.println("Is terminal toggled: " + isTerminalToggled + " (old: " + terminalShowing + ")");
    }

    /**
     * Hides the project view by setting its divider location.
     */
    public void hideProjectView() {
        projectEditorSplit.setDividerLocation(1);
    }

    /**
     * Hides the terminal view by setting its divider location to a minimal value.
     */
    public void hideTerminal() {
        SwingUtilities.invokeLater(() ->
                editorTerminalSplit.setDividerLocation(editorTerminalSplit.getMaximumDividerLocation() + 100)
        );
    }

    /**
     * Recursively disables floating toolbars in the specified component.
     *
     * @param component the root component to disable toolbars in.
     */
    private void disableFloatingToolbars(Component component) {
        if (component instanceof JToolBar) {
            ((JToolBar) component).setFloatable(false);
        } else if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                disableFloatingToolbars(child);
            }
        }
    }

    /**
     * Disables all toolbars in the given frame.
     *
     * @param frame the JFrame to disable toolbars in.
     */
    private void disableAllToolbars(JFrame frame) {
        disableFloatingToolbars(frame.getContentPane());
    }

    /**
     * Displays a tree view of the current source code, if available for the
     * current programming language.
     */
    public void refreshSourceTree() {
        SwingUtilities.invokeLater(() -> {
            if (sourceTree!= null) {
                sourceTree.uninstall();
            }

            String language = tabManager.getActiveTextArea().getSyntaxEditingStyle();
            switch (language) {
                case SYNTAX_STYLE_JAVA ->  {
                    sourceTree = new JavaOutlineTree();
                    LanguageSupport support = lsf.getSupportFor(SYNTAX_STYLE_JAVA);
                    JavaLanguageSupport jls = (JavaLanguageSupport) support;
                    support.install(tabManager.getActiveTextArea());
                }
                case SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT -> sourceTree = new JavaScriptOutlineTree();
                case SyntaxConstants.SYNTAX_STYLE_XML -> sourceTree = new XmlOutlineTree();
                case null, default -> sourceTree = null;
            }

            if (sourceTree != null) {
                sourceTree.listenTo(tabManager.getActiveTextArea());
                treeSP.setViewportView(sourceTree);
            } else {
                JTree dummy = new JTree((TreeNode) null);
                treeSP.setViewportView(dummy);
            }
            treeSP.revalidate();
        });
    }
}