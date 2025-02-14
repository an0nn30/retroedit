package com.github.an0nn30.editor.ui;

import com.github.an0nn30.editor.event.EventBus;
import com.github.an0nn30.editor.event.EventType;
import com.github.an0nn30.editor.settings.Constants;
import com.github.an0nn30.editor.ui.components.TextArea;
import com.github.an0nn30.editor.ui.utils.FileUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.*;
import java.awt.*;
import java.io.*;

public class TabManager extends JTabbedPane {

    private final Editor editor;

    public TabManager(Editor editor) {
        super(SwingConstants.TOP);
        this.editor = editor;
        EventBus.subscribe(EventType.TAB_UPDATED.name(), event -> setTitleAt(getSelectedIndex(), event.data().toString()));

    }

    // Creates a new TextArea and applies settings.
    private TextArea createTextArea() {
        TextArea textArea = new TextArea(editor);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        textArea.setCodeFoldingEnabled(true);
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { markModified(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { markModified(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { markModified(); }
        });
        // Apply the font size from Settings.
        Font font = textArea.getFont();
        textArea.setFont(new Font(font.getName(), font.getStyle(), Settings.getFontSize()));
        return textArea;

    }

    public TextArea getActiveTextArea() {
        Component comp = getSelectedComponent();
        if (comp instanceof JScrollPane scrollPane) {
            return (TextArea) scrollPane.getViewport().getView();
        }
        return null;
    }

    // Mark the current tab as modified.
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

    // Directly open a file in a new tab (or re-use an untitled tab).
    public void openFile(File file) {
        if (file == null) return;
        if (!confirmSaveIfNeeded()) return;
        if (checkAndSelectIfFileAlreadyOpen(file)) return;
        openFileInNewTab(file);
    }

    // Check if the file is already open in any tab.
    private boolean checkAndSelectIfFileAlreadyOpen(File file) {
        for (int i = 0; i < getTabCount(); i++) {
            Component comp = getComponentAt(i);
            if (comp instanceof JScrollPane) {
                TextArea ta = (TextArea) ((JScrollPane) comp).getViewport().getView();
                File openFile = FileUtils.getCurrentFile(ta);
                if (openFile != null) {
                    try {
                        if (openFile.getCanonicalPath().equals(file.getCanonicalPath())) {
                            setSelectedIndex(i);
                            EventBus.publish(EventType.TAB_UPDATED.name(), file.getName(), this);
                            return true;
                        }
                    } catch (IOException e) {
                        if (openFile.getAbsolutePath().equals(file.getAbsolutePath())) {
                            setSelectedIndex(i);
                            EventBus.publish(EventType.TAB_UPDATED.name(), file.getName(), this);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Open the file in a new tab.
    private void openFileInNewTab(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            TextArea newTextArea = createTextArea();
            newTextArea.setActiveFile(file);
            newTextArea.read(reader, null);

            // Determine syntax style from file extension.
            String extension = "";
            int dotIndex = file.getName().lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < file.getName().length() - 1) {
                extension = file.getName().substring(dotIndex + 1);
            }
            String syntax = Constants.supportedFileTypes.getOrDefault(extension, SyntaxConstants.SYNTAX_STYLE_NONE);
            // Use event bus only to update syntax highlighting.
            EventBus.publish(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(), syntax, this);

            int currentIndex = getSelectedIndex();
            String currentTitle = (currentIndex >= 0 ? getTitleAt(currentIndex) : "");
            if (currentIndex >= 0 && (currentTitle.isEmpty() || currentTitle.equals("Untitled") || currentTitle.startsWith("*Untitled"))) {
                setComponentAt(currentIndex, new RTextScrollPane(newTextArea));
                EventBus.publish(EventType.TAB_UPDATED.name(), file.getName(), this);
            } else {
                addNewTab(file.getName(), newTextArea);
                EventBus.publish(EventType.TAB_UPDATED.name(), file.getName(), this);
            }
            FileUtils.setCurrentFile(newTextArea, file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(editor, "Error opening file",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Save the current file.
    public void saveFile(boolean saveAs) {
        TextArea textArea = getActiveTextArea();
        if (textArea == null) return;
        File file = FileUtils.getCurrentFile(textArea);
        if (file == null || saveAs) {
            file = FileUtils.saveFileDialog(editor);
            if (file == null) return;
            FileUtils.setCurrentFile(textArea, file);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            textArea.write(writer);
            int index = getSelectedIndex();
            setTitleAt(index, file.getName());
            EventBus.publish(EventType.TAB_UPDATED.name(), file.getName(), this);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(editor, "Error saving file",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Close the current tab.
    public void closeCurrentTab() {
        int index = getSelectedIndex();
        if (index != -1 && confirmSaveIfNeeded()) {
            remove(index);
        }
    }

    // Adjust the font size in the active TextArea.
    public void adjustFontSize(int change) {
        TextArea textArea = getActiveTextArea();
        if (textArea != null) {
            Font font = textArea.getFont();
            int newSize = Math.max(font.getSize() + change, 8);
            textArea.setFont(new Font(font.getName(), font.getStyle(), newSize));
            Settings.setFontSize(newSize);
        }
    }

    // Navigate to the previous tab.
    public void previousTab() {
        int tabCount = getTabCount();
        if (tabCount > 0) {
            int currentIndex = getSelectedIndex();
            int previousIndex = (currentIndex - 1 + tabCount) % tabCount;
            setSelectedIndex(previousIndex);
            EventBus.publish(EventType.TAB_UPDATED.name(), getTitleAt(previousIndex), this);
        }
    }

    // Navigate to the next tab.
    public void nextTab() {
        int tabCount = getTabCount();
        if (tabCount > 0) {
            int currentIndex = getSelectedIndex();
            int nextIndex = (currentIndex + 1) % tabCount;
            setSelectedIndex(nextIndex);
            EventBus.publish(EventType.TAB_UPDATED.name(), getTitleAt(nextIndex), this);
        }
    }

    // Add a new tab.
    public void addNewTab(String title, TextArea textArea) {
        if (textArea == null) {
            textArea = createTextArea();
        }
        JScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        addTab(title, scrollPane);
        setSelectedIndex(getTabCount() - 1);
        EventBus.publish(EventType.TAB_UPDATED.name(), title, this);
    }

    // Ask to save if the current tab has unsaved changes.
    private boolean confirmSaveIfNeeded() {
        int index = getSelectedIndex();
        if (index != -1) {
            String title = getTitleAt(index);
            if (title.startsWith("*")) {
                int choice = JOptionPane.showConfirmDialog(editor,
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
