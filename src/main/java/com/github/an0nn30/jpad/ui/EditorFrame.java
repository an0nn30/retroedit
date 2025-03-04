package com.github.an0nn30.jpad.ui;

import com.github.an0nn30.jpad.autocomplete.SourceTreeRefresher;
import com.github.an0nn30.jpad.event.EventBus;
import com.github.an0nn30.jpad.event.EventType;
import com.github.an0nn30.jpad.launchers.LaunchConfigManager;
import com.github.an0nn30.jpad.settings.Settings;
import com.github.an0nn30.jpad.ui.components.DirectoryTree;
import com.github.an0nn30.jpad.ui.components.TextArea;
import com.github.an0nn30.jpad.ui.platform.MacUtils;
import com.github.an0nn30.jpad.ui.search.SearchController;
import com.github.an0nn30.jpad.ui.theme.ThemeManager;
import org.fife.rsta.ac.AbstractSourceTree;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.c.CLanguageSupport;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rsta.ac.java.tree.JavaOutlineTree;
import org.fife.rsta.ac.js.tree.JavaScriptOutlineTree;
import org.fife.rsta.ac.xml.tree.XmlOutlineTree;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_JAVA;

/**
 * The main editor frame for the Retro Edit application.
 */
public class EditorFrame extends JFrame {

    // Keep track of all open editor windows.
    private static final List<EditorFrame> openFrames = new ArrayList<>();

    private MainToolbar mainToolbar;
    private TextAreaTabManager textAreaTabManager;
    private JSplitPane projectEditorSplit;
    private JSplitPane editorTerminalSplit;
    private DirectoryTree directoryTree;
    private boolean isProjectViewToggled = false;
    private boolean isTerminalToggled = false; // false means terminal is minimized/hidden
    private SearchController searchController;
    private int projectViewDividerLocation = 200;
    private int terminalViewDividerLocation = 150; // default initial value when expanded
    private AbstractSourceTree sourceTree;
    private JScrollPane treeSP;
    LanguageSupportFactory lsf;
    private boolean createUntitledTab = true;
    private File openFile;
    private LaunchConfigManager launchConfigManager;
    private TerminalTabManager terminalTabManager;

    public EditorFrame(File openFile) {
        super("Retro Edit");
        this.openFile = openFile;
        if (openFile.exists() && openFile.isFile()) {
            this.createUntitledTab = false;
        }
       
        // Add this frame to the list of open windows.
        openFrames.add(this);
        // When this window is closed, remove it from the list.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                openFrames.remove(EditorFrame.this);
            }
        });
        // Install a global key event dispatcher so that cmd+shift+, toggles the terminal view,
        // even if the terminal widget currently has focus.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                int expectedModifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK;
                if (e.getKeyCode() == KeyEvent.VK_COMMA &&
                        (e.getModifiersEx() & expectedModifiers) == expectedModifiers) {
                    toggleTerminalView();
                    return true; // Consume the event so that it does not reach the terminal widget.
                }
            }
            return false;
        });

        // Initialize everything directly on the main thread.
        initializeFrame();
        initializeComponents();
        layoutComponents();
        registerEventSubscriptions();
        startEditor();
        disableAllToolbars(this);
        
    }

    /**
     * Constructs the EditorFrame and initializes the UI.
     */
    public EditorFrame() {
        super("Retro Edit");
        // Add this frame to the list of open windows.
        openFrames.add(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                openFrames.remove(EditorFrame.this);
            }
        });
        // Install a global key event dispatcher so that cmd+shift+, toggles the terminal view,
        // even if the terminal widget currently has focus.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                int expectedModifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK;
                if (e.getKeyCode() == KeyEvent.VK_COMMA &&
                        (e.getModifiersEx() & expectedModifiers) == expectedModifiers) {
                    toggleTerminalView();
                    return true; // Consume the event so that it does not reach the terminal widget.
                }
            }
            return false;
        });

        ThemeManager.setupWindowFrame(this, Settings.getInstance().getInterfaceTheme());
        // Initialize everything directly on the main thread.
        initializeFrame();
        initializeComponents();
        layoutComponents();
        registerEventSubscriptions();
        startEditor();
        disableAllToolbars(this);
       	
    }

    /**
     * Initializes frame properties such as size and close operation.
     */
    private void initializeFrame() {
        MacUtils macUtils = new MacUtils();
        macUtils.setMacTitleBar(this);
        setSize(900, 900);
        // Use DISPOSE_ON_CLOSE so that closing one window doesn't exit the application.
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Initializes UI components used in the frame.
     */
    private void initializeComponents() {
        launchConfigManager = new LaunchConfigManager(this);
        mainToolbar = new MainToolbar(this, launchConfigManager);
        textAreaTabManager = new TextAreaTabManager(this);
        terminalTabManager = new TerminalTabManager(this);
        setJMenuBar(new MenuBar(this).getMenuBar());
        JTree dummy = new JTree((TreeNode) null);
        treeSP = new JScrollPane(dummy);
        directoryTree = new DirectoryTree(this);
        if (this.openFile != null && this.openFile.isDirectory())
            directoryTree.setRootDirectory(this.openFile);
        searchController = null; // Lazy-load search controller
        lsf = LanguageSupportFactory.get();
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

        // Hide views immediately.
        hideProjectView();
        hideTerminal();
    }

    /**
     * Creates the split pane for the editor and terminal.
     *
     * @return a configured JSplitPane for the editor and terminal.
     */
    private JSplitPane createEditorTerminalSplit() {
        // Remove automatic resizing by setting resize weight to 0.
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textAreaTabManager, terminalTabManager);
        split.setResizeWeight(0);
        split.setOneTouchExpandable(false);
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
                    // Collapse the terminal by computing its minimized position.
                    split.setDividerLocation(split.getHeight() - split.getDividerSize());
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
        split.setResizeWeight(0.8);
        JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split, editorTerminalSplit);
        // Remove automatic resizing by setting resize weight to 0.
        split2.setResizeWeight(1);
        split2.setOneTouchExpandable(false);
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
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            TitleBar titleBar = new TitleBar(this, mainToolbar);
            add(titleBar, BorderLayout.NORTH);
        } else {
            add(mainToolbar, BorderLayout.NORTH);
        }
        add(projectEditorSplit, BorderLayout.CENTER);
    }

    /**
     * Registers event subscriptions for the frame.
     */
    private void registerEventSubscriptions() {
        EventBus.subscribe(EventType.TAB_UPDATED.name(), eventRecord -> {
            refreshSourceTree();
            setTitle(eventRecord.data().toString());
        });
        EventBus.subscribe(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(), eventRecord -> {
            refreshSourceTree();
        });
        EventBus.subscribe(EventType.THEME_CHANGED.name(), eventRecord ->
                ThemeManager.updateInterfaceTheme(this, eventRecord.data()));
    }

    /**
     * Starts the editor by adding an initial tab and applying theme settings.
     * Note: The terminal will start in the minimized (hidden) state.
     */
    private void startEditor() {
        if (openFile != null && openFile.isFile())
            textAreaTabManager.openFile(this.openFile);
        else if (openFile != null && !openFile.exists())
            textAreaTabManager.openFile(this.openFile);
        else
            textAreaTabManager.addNewTab("Untitled", new TextArea(this));
        // Instead of toggling the terminal view (which would show it), we keep it minimized.
        hideTerminal();
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
    public TextAreaTabManager getTabManager() {
        return textAreaTabManager;
    }

    /**
     * Returns the directory tree.
     *
     * @return the DirectoryTree instance.
     */
    public DirectoryTree getDirectoryTree() {
        return directoryTree;
    }

    public TerminalTabManager getTerminalTabManager() {
        return terminalTabManager;
    }

    /**
     * Opens the specified file in a new tab.
     *
     * @param file the file to open.
     */
    public void openFileInNewTab(File file) {
        textAreaTabManager.openFile(file);
    }

    /**
     * Returns an existing open EditorFrame if one exists; otherwise returns null.
     */
    public static EditorFrame getAnyOpenFrame() {
        return openFrames.isEmpty() ? null : openFrames.get(0);
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
            textAreaTabManager.getActiveTextArea().requestFocus();
        }
    }

    /**
     * Toggles the visibility of the terminal view.
     */
    public void toggleTerminalView() {
        isTerminalToggled = !isTerminalToggled;
        System.out.println(isTerminalToggled);
        if (isTerminalToggled) {
            // When expanding, reapply the stored divider location.
            editorTerminalSplit.setDividerLocation(terminalViewDividerLocation);
            if (terminalTabManager.getActiveTerminal() != null) {
                terminalTabManager.getActiveTerminal().requestFocus();
            }
        } else {
            // When collapsing, set divider location so that the top component occupies full height.
            editorTerminalSplit.setDividerLocation(editorTerminalSplit.getHeight() - editorTerminalSplit.getDividerSize());
            textAreaTabManager.getActiveTextArea().requestFocus();
        }
    }

    /**
     * Hides the project view by setting its divider location.
     */
    public void hideProjectView() {
        projectEditorSplit.setDividerLocation(1);
    }

    /**
     * Hides (minimizes) the terminal view by setting its divider location dynamically.
     * Also updates the terminal state flag to indicate it is hidden.
     */
    public void hideTerminal() {
        isTerminalToggled = false;
        SwingUtilities.invokeLater(() -> {
            int minimizedLocation = editorTerminalSplit.getHeight() - editorTerminalSplit.getDividerSize();
            editorTerminalSplit.setDividerLocation(minimizedLocation);
        });
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
     * Delegates the refreshing of the source tree to SourceTreeRefresher.
     */
    public void refreshSourceTree() {
        SourceTreeRefresher refresher = new SourceTreeRefresher(textAreaTabManager, directoryTree, treeSP);
        refresher.refresh();
    }

    public boolean getIsTerminalToggled() {
        return isTerminalToggled;
    }
}