package com.github.an0nn30.ui;

import com.github.an0nn30.utils.FileUtils;
import com.github.an0nn30.utils.ThemeUtils;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class EditorTabManager {
    private final JTabbedPane tabbedPane;
    private final EditorFrame editorFrame;
    private Map<String, String> syntaxMap;

    public EditorTabManager(EditorFrame frame) {
        this.editorFrame = frame;
        tabbedPane = new JTabbedPane(SwingConstants.TOP);
        initSyntaxMap();
    }

    private void initSyntaxMap() {
        syntaxMap = new HashMap<>();
        syntaxMap.put("java", SyntaxConstants.SYNTAX_STYLE_JAVA);
        syntaxMap.put("py", SyntaxConstants.SYNTAX_STYLE_PYTHON);
        syntaxMap.put("c", SyntaxConstants.SYNTAX_STYLE_C);
        syntaxMap.put("cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
        syntaxMap.put("json", SyntaxConstants.SYNTAX_STYLE_JSON);
        syntaxMap.put("go", SyntaxConstants.SYNTAX_STYLE_GO);
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * Adds a new tab. If no text area is supplied, a new one is created.
     */
    public void addNewTab(String title, RSyntaxTextArea textArea) {
        if (textArea == null) {
            textArea = createTextArea();
        }
        JScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        tabbedPane.setUI(new com.formdev.flatlaf.ui.FlatTabbedPaneUI() {
            @Override
            protected Insets getTabAreaInsets(int tabPlacement) {
                Insets insets = super.getTabAreaInsets(tabPlacement);
                // Increase the left inset by 50 pixels
                insets.left += 65;
                return insets;
            }
        });

        updateTabTitle();
    }

    private RSyntaxTextArea createTextArea() {
        RSyntaxTextArea textArea = new RSyntaxTextArea();
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
        // Apply the default theme from our utils/theme manager.
        ThemeUtils.applyDefaultTheme(textArea);
        // Optionally, set the default font from Settings.
        textArea.setFont(new Font(editorFrame.getSettings().getFontName(),
                Font.PLAIN, editorFrame.getSettings().getFontSize()));
        return textArea;
    }

    private void markModified() {
        int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            String title = tabbedPane.getTitleAt(index);
            if (!title.startsWith("*")) {
                tabbedPane.setTitleAt(index, "*" + title);
            }
        }
    }

    /**
     * If the current tab is modified (its title begins with "*") then ask
     * whether to save the file.
     */
    private boolean confirmSaveIfNeeded() {
        int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            String title = tabbedPane.getTitleAt(index);
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

    public RSyntaxTextArea getActiveTextArea() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp instanceof JScrollPane scrollPane) {
            return (RSyntaxTextArea) scrollPane.getViewport().getView();
        }
        return null;
    }

    public void openFile() {
        RSyntaxTextArea activeTextArea = getActiveTextArea();
        if (activeTextArea == null) return;
        if (!confirmSaveIfNeeded()) return;
        File file = FileUtils.openFileDialog(editorFrame);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                RSyntaxTextArea newTextArea = createTextArea();
                newTextArea.read(reader, null);
                // Set syntax highlighting based on file extension.
                applySyntaxHighlighting(newTextArea, file);

                // Replace the current tab if it is an untitled one;
                int currentIndex = tabbedPane.getSelectedIndex();
                String currentTitle = tabbedPane.getTitleAt(currentIndex);
                if ("Untitled".equals(currentTitle) || currentTitle.startsWith("*Untitled")) {
                    tabbedPane.setComponentAt(currentIndex, new RTextScrollPane(newTextArea));
                    tabbedPane.setTitleAt(currentIndex, file.getName());
                } else {
                    addNewTab(file.getName(), newTextArea);
                }
                // Store the current file on the text area (using a client property)
                FileUtils.setCurrentFile(newTextArea, file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(editorFrame, "Error opening file",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void openFile(File file) {
        if (!confirmSaveIfNeeded()) return;
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                RSyntaxTextArea newTextArea = createTextArea();
                newTextArea.read(reader, null);
                // Apply syntax highlighting based on file extension.
                applySyntaxHighlighting(newTextArea, file);

                // Replace the current tab if it is an untitled one.
                int currentIndex = tabbedPane.getSelectedIndex();
                String currentTitle = tabbedPane.getTitleAt(currentIndex);
                if ("Untitled".equals(currentTitle) || currentTitle.startsWith("*Untitled")) {
                    tabbedPane.setComponentAt(currentIndex, new RTextScrollPane(newTextArea));
                    tabbedPane.setTitleAt(currentIndex, file.getName());
                } else {
                    addNewTab(file.getName(), newTextArea);
                }
                // Associate this file with the text area for later saving.
                com.github.an0nn30.utils.FileUtils.setCurrentFile(newTextArea, file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(editorFrame,
                        "Error opening file",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveFile(boolean saveAs) {
        RSyntaxTextArea textArea = getActiveTextArea();
        if (textArea == null) return;
        File file = FileUtils.getCurrentFile(textArea);
        if (file == null || saveAs) {
            file = FileUtils.saveFileDialog(editorFrame);
            if (file == null) return;
            FileUtils.setCurrentFile(textArea, file);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            textArea.write(writer);
            // Remove the “modified” marker from the tab title.
            int index = tabbedPane.getSelectedIndex();
            tabbedPane.setTitleAt(index, file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(editorFrame, "Error saving file",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void closeCurrentTab() {
        int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            if (confirmSaveIfNeeded()) {
                tabbedPane.remove(index);
            }
        }
    }

    public void adjustFontSize(int change) {
        RSyntaxTextArea textArea = getActiveTextArea();
        if (textArea != null) {
            Font font = textArea.getFont();
            int newSize = Math.max(font.getSize() + change, 8);
            textArea.setFont(new Font(font.getName(), font.getStyle(), newSize));
        }
    }

    private void applySyntaxHighlighting(RSyntaxTextArea textArea, File file) {
        if (file != null) {
            String fileName = file.getName();
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
                extension = fileName.substring(dotIndex + 1);
            }
            String syntax = syntaxMap.getOrDefault(extension, SyntaxConstants.SYNTAX_STYLE_NONE);
            textArea.setSyntaxEditingStyle(syntax);
            // Update status bar (if desired)
            editorFrame.getStatusBar().setText("File Type: " + extension);
        }
    }

    private void updateTabTitle() {
        int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            String title = tabbedPane.getTitleAt(index);
            tabbedPane.setTitleAt(index, title);
        }
    }
}