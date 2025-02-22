package com.github.an0nn30.retroedit.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.an0nn30.retroedit.event.EventBus;
import com.github.an0nn30.retroedit.event.EventType;
import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Constants;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.components.TextArea;
import com.github.an0nn30.retroedit.ui.theme.ThemeManager;
import com.github.an0nn30.retroedit.ui.utils.FileUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * Manages tabs within the editor. Each tab contains a {@link TextArea} for editing text files.
 * This class handles creating new tabs, opening files, saving files, and updating tab states.
 * Each tab uses a prefixed icon loaded from the resource path "/tango/document-new.svg".
 */
public class TabManager extends JTabbedPane {

    private final EditorFrame editorFrame;

    // Load the tab icon from resources using FlatLaf's SVG support.
    private static final Icon TAB_ICON = loadTabIcon();

    /**
     * Constructs a new TabManager for the given EditorFrame.
     *
     * @param editorFrame the parent EditorFrame.
     */
    public TabManager(EditorFrame editorFrame) {
        super(SwingConstants.TOP);
        this.editorFrame = editorFrame;
        subscribeToTabUpdateEvents();
    }

    /**
     * Subscribes to TAB_UPDATED events to update the title of the selected tab.
     */
    private void subscribeToTabUpdateEvents() {
        EventBus.subscribe(EventType.TAB_UPDATED.name(), event ->
                setTitleAt(getSelectedIndex(), event.data().toString()));
    }

    /**
     * Loads the tab icon from the resource path "/tango/document-new.svg" using FlatSVGIcon.
     *
     * @return the loaded Icon, or null if loading fails.
     */
    private static Icon loadTabIcon() {
        try {
            return new FlatSVGIcon(ThemeManager.icons.get("file"), 20, 20);
        } catch (Exception e) {
            Logger.getInstance().error(TabManager.class, "Error loading tab icon: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a new {@link TextArea} with applied settings and event listeners.
     *
     * @return a configured TextArea instance.
     */
    private TextArea createTextArea() {
        TextArea textArea = new TextArea(editorFrame);
        applyTheme(textArea);
        configureTextArea(textArea);
        return textArea;
    }

    /**
     * Applies the appropriate theme to the given TextArea based on current settings.
     *
     * @param textArea the TextArea to which the theme will be applied.
     */
    private void applyTheme(TextArea textArea) {
        String interfaceTheme = Settings.getInstance().getInterfaceTheme();
        String themePath = "/org/fife/ui/rsyntaxtextarea/themes/idea.xml"; // default

        if (interfaceTheme.equalsIgnoreCase("dark")) {
            themePath = "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";
        }
        try (InputStream is = getClass().getResourceAsStream(themePath)) {
            if (is != null) {
                Theme theme = Theme.load(is);
                theme.apply(textArea);
            } else {
                Logger.getInstance().error(getClass(), "Theme resource not found: " + themePath);
            }
        } catch (IOException e) {
            Logger.getInstance().error(getClass(), "Error loading theme: " + e.getMessage());
        }
    }

    /**
     * Configures the TextArea with syntax settings, code folding, font settings, and a document listener for modifications.
     *
     * @param textArea the TextArea to be configured.
     */
    private void configureTextArea(TextArea textArea) {
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        textArea.setCodeFoldingEnabled(true);
        textArea.initFontSizeAndFamily();
        attachModificationListener(textArea);

        Font font = textArea.getFont();
        textArea.setFont(new Font(font.getName(), font.getStyle(), Settings.getInstance().getEditorFontSize()));
    }

    /**
     * Attaches a document listener to the TextArea to mark the tab as modified on changes.
     *
     * @param textArea the TextArea to attach the listener to.
     */
    private void attachModificationListener(TextArea textArea) {
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                markModified();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                markModified();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                markModified();
            }
        });
    }

    /**
     * Returns the active TextArea from the currently selected tab.
     *
     * @return the active TextArea, or null if none is active.
     */
    public TextArea getActiveTextArea() {
        Component comp = getSelectedComponent();
        if (comp instanceof JScrollPane scrollPane) {
            return (TextArea) scrollPane.getViewport().getView();
        }
        return null;
    }

    /**
     * Marks the current tab as modified by prefixing an asterisk (*) to the tab title.
     */
    public void markModified() {
        int index = getSelectedIndex();
        if (index != -1) {
            String title = getTitleAt(index);
            if (!title.startsWith("*")) {
                String newTitle = "*" + title;
                setTitleAt(index, newTitle);
                EventBus.publish(EventType.TAB_UPDATED.name(), newTitle, this);
            }
        }
    }

    /**
     * Opens a file in a new tab, or selects the tab if the file is already open.
     *
     * @param file the file to open.
     */
    public void openFile(File file) {
        if (file == null) return;
        if (!confirmSaveIfNeeded()) return;
        if (checkAndSelectIfFileAlreadyOpen(file)) return;
        openFileInNewTab(file);
        requestFocusOnActiveTextArea();
    }

    /**
     * Requests focus on the active TextArea in the current tab.
     */
    private void requestFocusOnActiveTextArea() {
        TextArea activeTextArea = getActiveTextArea();
        if (activeTextArea != null) {
            activeTextArea.requestFocus();
        }
    }

    /**
     * Checks if the file is already open in any tab. If found, selects that tab.
     *
     * @param file the file to check.
     * @return true if the file is already open, false otherwise.
     */
    private boolean checkAndSelectIfFileAlreadyOpen(File file) {
        for (int i = 0; i < getTabCount(); i++) {
            Component comp = getComponentAt(i);
            if (comp instanceof JScrollPane scrollPane) {
                TextArea ta = (TextArea) scrollPane.getViewport().getView();
                File openFile = FileUtils.getCurrentFile(ta);
                if (openFile != null && filesAreEqual(openFile, file)) {
                    setSelectedIndex(i);
                    EventBus.publish(EventType.TAB_UPDATED.name(), file.getName(), this);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares two files for equality based on canonical paths, falling back to absolute paths if necessary.
     *
     * @param f1 the first file.
     * @param f2 the second file.
     * @return true if the files are considered equal, false otherwise.
     */
    private boolean filesAreEqual(File f1, File f2) {
        try {
            return f1.getCanonicalPath().equals(f2.getCanonicalPath());
        } catch (IOException e) {
            return f1.getAbsolutePath().equals(f2.getAbsolutePath());
        }
    }

    /**
     * Opens the given file in a new tab.
     *
     * @param file the file to open.
     */
    private void openFileInNewTab(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            TextArea newTextArea = createTextArea();
            newTextArea.setActiveFile(file);
            newTextArea.read(reader, null);

            String syntax = determineSyntaxFromFile(file);
            EventBus.publish(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(), syntax, this);

            if (shouldReplaceCurrentTab()) {
                replaceCurrentTab(file.getName(), newTextArea);
            } else {
                addNewTab(file.getName(), newTextArea);
            }
            FileUtils.setCurrentFile(newTextArea, file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(editorFrame, "Error opening file",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Determines the syntax style for a file based on its extension.
     *
     * @param file the file whose extension will be used.
     * @return the syntax style constant.
     */
    private String determineSyntaxFromFile(File file) {
        String extension = getFileExtension(file);
        return Constants.supportedFileTypes.getOrDefault(extension, SyntaxConstants.SYNTAX_STYLE_NONE);
    }

    /**
     * Returns the file extension for the given file.
     *
     * @param file the file from which to extract the extension.
     * @return the file extension as a String, or an empty string if none exists.
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < name.length() - 1) {
            return name.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * Determines if the current tab should be replaced with the new file.
     * Replaces the tab if it is empty, untitled, or a modified untitled tab.
     *
     * @return true if the current tab should be replaced, false otherwise.
     */
    private boolean shouldReplaceCurrentTab() {
        int currentIndex = getSelectedIndex();
        if (currentIndex >= 0) {
            String currentTitle = getTitleAt(currentIndex);
            return currentTitle.isEmpty() ||
                    currentTitle.equals("Untitled") ||
                    currentTitle.startsWith("*Untitled");
        }
        return false;
    }

    /**
     * Replaces the current tab with a new {@link RTextScrollPane} wrapping the provided TextArea.
     *
     * @param title       the title for the tab.
     * @param newTextArea the TextArea to display.
     */
    private void replaceCurrentTab(String title, TextArea newTextArea) {
        int currentIndex = getSelectedIndex();
        setComponentAt(currentIndex, new RTextScrollPane(newTextArea));
        // Set the icon on the tab along with the title.
        setIconAt(currentIndex, TAB_ICON);
        EventBus.publish(EventType.TAB_UPDATED.name(), title, this);
    }

    /**
     * Saves the file in the active tab. If saveAs is true or no file is associated,
     * a save dialog is shown.
     *
     * @param saveAs if true, forces the save dialog to appear.
     */
    public void saveFile(boolean saveAs) {
        TextArea textArea = getActiveTextArea();
        if (textArea == null) return;
        File file = FileUtils.getCurrentFile(textArea);
        if (file == null || saveAs) {
            file = FileUtils.saveFileDialog(editorFrame);
            if (file == null) return;
            FileUtils.setCurrentFile(textArea, file);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            textArea.write(writer);
            int index = getSelectedIndex();
            setTitleAt(index, file.getName());
            EventBus.publish(EventType.TAB_UPDATED.name(), file.getName(), this);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(editorFrame, "Error saving file",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Closes the currently selected tab after confirming save if needed.
     */
    public void closeCurrentTab() {
        int index = getSelectedIndex();
        if (index != -1 && confirmSaveIfNeeded()) {
            remove(index);
        }
    }

    /**
     * Adjusts the font size in the active TextArea by the given change.
     *
     * @param change the change in font size (positive or negative).
     */
    public void adjustFontSize(int change) {
        TextArea textArea = getActiveTextArea();
        if (textArea != null) {
            Font font = textArea.getFont();
            int newSize = Math.max(font.getSize() + change, 8);
            textArea.setFont(new Font(font.getName(), font.getStyle(), newSize));
            Settings.getInstance().setEditorFontSize(newSize);
        }
    }

    /**
     * Navigates to the previous tab.
     */
    public void previousTab() {
        int tabCount = getTabCount();
        if (tabCount > 0) {
            int currentIndex = getSelectedIndex();
            int previousIndex = (currentIndex - 1 + tabCount) % tabCount;
            setSelectedIndex(previousIndex);
            EventBus.publish(EventType.TAB_UPDATED.name(), getTitleAt(previousIndex), this);
        }
    }

    /**
     * Navigates to the next tab.
     */
    public void nextTab() {
        int tabCount = getTabCount();
        if (tabCount > 0) {
            int currentIndex = getSelectedIndex();
            int nextIndex = (currentIndex + 1) % tabCount;
            setSelectedIndex(nextIndex);
            EventBus.publish(EventType.TAB_UPDATED.name(), getTitleAt(nextIndex), this);
        }
    }

    /**
     * Adds a new tab with the specified title and TextArea. If the provided TextArea is null,
     * a new one is created. The new tab uses a prefixed icon loaded from "/tango/document-new.svg".
     *
     * @param title    the title of the new tab.
     * @param textArea the TextArea to display, or null to create a new one.
     */
    public void addNewTab(String title, TextArea textArea) {
        if (textArea == null) {
            textArea = createTextArea();
        }
        JScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Use the overloaded addTab method with an icon.
        addTab(title, TAB_ICON, scrollPane, null);
        setSelectedIndex(getTabCount() - 1);
        EventBus.publish(EventType.TAB_UPDATED.name(), title, this);
    }

    /**
     * Prompts the user to save changes if the current tab is marked as modified.
     *
     * @return true if the user chooses to proceed, false if the action is cancelled.
     */
    private boolean confirmSaveIfNeeded() {
        int index = getSelectedIndex();
        if (index != -1) {
            String title = getTitleAt(index);
            if (title.startsWith("*")) {
                int choice = JOptionPane.showConfirmDialog(editorFrame,
                        "You have unsaved changes. Save now?",
                        "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
                if (choice == JOptionPane.CANCEL_OPTION) {
                    return false;
                }
                if (choice == JOptionPane.YES_OPTION) {
                    saveFile(false);
                }
            }
        }
        return true;
    }
}