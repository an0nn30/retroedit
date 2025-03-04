package com.github.an0nn30.jpad.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.an0nn30.jpad.event.EventBus;
import com.github.an0nn30.jpad.event.EventType;
import com.github.an0nn30.jpad.logging.Logger;
import com.github.an0nn30.jpad.settings.Settings;
import com.github.an0nn30.jpad.ui.components.TextArea;
import com.github.an0nn30.jpad.ui.theme.ThemeManager;
import com.github.an0nn30.jpad.ui.utils.FileUtils;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;

/**
 * Manages tabs within the editor. Each tab contains a {@link TextArea} for editing text files.
 * This version extends {@code BaseTabManager} so that common focus tracking logic is shared.
 */
public class TextAreaTabManager extends BaseTabManager<TextArea> {

    private final EditorFrame editorFrame;
    private static final Icon TAB_ICON = loadTabIcon();

    public TextAreaTabManager(EditorFrame editorFrame) {
        super(SwingConstants.TOP);
        this.editorFrame = editorFrame;
        subscribeToTabUpdateEvents();

        // When switching tabs, refresh the source tree using the active TextArea.
        addChangeListener(e -> {
            editorFrame.refreshSourceTree();
            TextArea activeTextArea = getActiveTextArea();
            if (activeTextArea != null) {
                File currentFile = FileUtils.getCurrentFile(activeTextArea);
                if (currentFile != null && editorFrame.getDirectoryTree() != null
                        && editorFrame.getDirectoryTree().getRootDirectory() != null) {
                    editorFrame.getDirectoryTree().selectFile(currentFile);
                }
            }
        });
    }

    private static Icon loadTabIcon() {
        try {
            return new FlatSVGIcon(ThemeManager.retroThemeIcons.get("empty-type"), 20, 20);
        } catch (Exception e) {
            Logger.getInstance().error(TextAreaTabManager.class, "Error loading tab icon: " + e.getMessage());
            return null;
        }
    }

    private void subscribeToTabUpdateEvents() {
        EventBus.subscribe(EventType.TAB_UPDATED.name(), event ->
                setTitleAt(getSelectedIndex(), event.data().toString()));
    }

    /**
     * Creates a new TextArea with theme and configuration applied.
     */
    private TextArea createTextArea() {
        TextArea textArea = new TextArea(editorFrame);
        applyTheme(textArea);
        configureTextArea(textArea);
        return textArea;
    }

    private void applyTheme(TextArea textArea) {
        String interfaceTheme = Settings.getInstance().getInterfaceTheme();
        String themePath = "/org/fife/ui/rsyntaxtextarea/themes/idea.xml"; // default

        if (interfaceTheme.equalsIgnoreCase("dark")) {
            themePath = "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";
        }
        try (InputStream is = getClass().getResourceAsStream(themePath)) {
            if (is != null) {
                org.fife.ui.rsyntaxtextarea.Theme theme = org.fife.ui.rsyntaxtextarea.Theme.load(is);
                theme.apply(textArea);
            } else {
                Logger.getInstance().error(getClass(), "Theme resource not found: " + themePath);
            }
        } catch (IOException e) {
            Logger.getInstance().error(getClass(), "Error loading theme: " + e.getMessage());
        }
    }

    private void configureTextArea(TextArea textArea) {
        textArea.setCodeFoldingEnabled(true);
        textArea.initFontSizeAndFamily();
        attachModificationListener(textArea);

        Font font = textArea.getFont();
        textArea.setFont(new Font(font.getName(), font.getStyle(), Settings.getInstance().getEditorFontSize()));
    }

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
     * Overrides the base method to wrap the TextArea in a scroll pane and attach a focus listener.
     */
    @Override
    protected void addComponentTab(String title, TextArea textArea) {
        // Attach a focus listener to update the last-focused TextArea.
        textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                lastFocusedComponent = textArea;
            }
        });
        // Wrap the TextArea in a scroll pane.
        JScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        addTab(title, TAB_ICON, scrollPane, null);
        setSelectedIndex(getTabCount() - 1);
        EventBus.publish(EventType.TAB_UPDATED.name(), title, this);
    }

    /**
     * Returns the active TextArea from the currently selected tab.
     * If the selected component is a scroll pane, its viewport is unwrapped.
     */
    public TextArea getActiveTextArea() {
        Component comp = getSelectedComponent();
        if (comp instanceof JScrollPane scrollPane) {
            return (TextArea) scrollPane.getViewport().getView();
        }
        return lastFocusedComponent;
    }

    /**
     * Adds a new tab for a TextArea. If the provided TextArea is null, one is created.
     */
    public void addNewTab(String title, TextArea textArea) {
        if (textArea == null) {
            textArea = createTextArea();
        }
        addComponentTab(title, textArea);
    }

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

    public void openFile(File file) {
        if (file == null) return;
        if (!confirmSaveIfNeeded()) return;
        if (checkAndSelectIfFileAlreadyOpen(file)) return;
        openFileInNewTab(file);
        requestFocusOnActiveTextArea();
        if (editorFrame.getDirectoryTree() != null
                && editorFrame.getDirectoryTree().getRootDirectory() != null) {
            editorFrame.getDirectoryTree().selectFile(file);
        }
    }

    public void setSelectedTabByTitle(String title) {
        for (int i = 0; i < getTabCount(); i++) {
            if (getTitleAt(i).equals(title)) {
                setSelectedIndex(i);
                return;
            }
        }
    }

    private void requestFocusOnActiveTextArea() {
        TextArea activeTextArea = getActiveTextArea();
        if (activeTextArea != null) {
            activeTextArea.requestFocus();
        }
    }

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

    private boolean filesAreEqual(File f1, File f2) {
        try {
            return f1.getCanonicalPath().equals(f2.getCanonicalPath());
        } catch (IOException e) {
            return f1.getAbsolutePath().equals(f2.getAbsolutePath());
        }
    }

    private void openFileInNewTab(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not create new file: " + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            TextArea newTextArea = createTextArea();
            newTextArea.setActiveFile(file);
            newTextArea.read(reader, null);

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

    private void replaceCurrentTab(String title, TextArea newTextArea) {
        int currentIndex = getSelectedIndex();
        setComponentAt(currentIndex, new RTextScrollPane(newTextArea));
        setIconAt(currentIndex, TAB_ICON);
        EventBus.publish(EventType.TAB_UPDATED.name(), title, this);
    }

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

    public void closeCurrentTab() {
        int index = getSelectedIndex();
        if (index != -1 && confirmSaveIfNeeded()) {
            remove(index);
        }
    }

    public void adjustFontSize(int change) {
        TextArea textArea = getActiveTextArea();
        if (textArea != null) {
            Font font = textArea.getFont();
            int newSize = Math.max(font.getSize() + change, 8);
            textArea.setFont(new Font(font.getName(), font.getStyle(), newSize));
            Settings.getInstance().setEditorFontSize(newSize);
        }
    }

    public void previousTab() {
        int tabCount = getTabCount();
        if (tabCount > 0) {
            int currentIndex = getSelectedIndex();
            int previousIndex = (currentIndex - 1 + tabCount) % tabCount;
            setSelectedIndex(previousIndex);
            EventBus.publish(EventType.TAB_UPDATED.name(), getTitleAt(previousIndex), this);
        }
    }

    public void nextTab() {
        int tabCount = getTabCount();
        if (tabCount > 0) {
            int currentIndex = getSelectedIndex();
            int nextIndex = (currentIndex + 1) % tabCount;
            setSelectedIndex(nextIndex);
            EventBus.publish(EventType.TAB_UPDATED.name(), getTitleAt(nextIndex), this);
        }
    }

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