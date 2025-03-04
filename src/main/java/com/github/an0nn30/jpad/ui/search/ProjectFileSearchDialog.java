package com.github.an0nn30.jpad.ui.search;

import com.github.an0nn30.jpad.ui.EditorFrame;
import com.github.an0nn30.jpad.ui.TextAreaTabManager;
import com.github.an0nn30.jpad.ui.components.DirectoryTree;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog that provides project-level file search functionality.
 * It uses a prebuilt search index (from ProjectFileSearchIndex) and precomputes
 * a lower-case search key for each file so that filtering happens instantly.
 * Filtering is now done asynchronously after a short pause in typing.
 */
public class ProjectFileSearchDialog extends JDialog {

    private final EditorFrame editorFrame;
    private final JTextField searchField;
    private final JList<File> fileList;
    private final DefaultListModel<File> listModel;
    // Precomputed list of indexed file entries (each with a file and its lower-case search key)
    private List<IndexedFileEntry> indexedEntries;
    // Timer for delaying the filtering until the user pauses typing.
    private Timer searchTimer;
    private static final int SEARCH_DELAY = 300; // milliseconds
    // Tracks the last query that triggered a filtering operation.
    private volatile String lastSearchQuery = "";

    /**
     * Helper class that stores a File and a precomputed lower-case search key.
     */
    private static class IndexedFileEntry {
        final File file;
        final String searchKey;

        IndexedFileEntry(File file) {
            this.file = file;
            // Precompute a search key using the file's name and its full path in lower-case.
            this.searchKey = file.getName().toLowerCase() + " " + file.getAbsolutePath().toLowerCase();
        }
    }

    public ProjectFileSearchDialog(EditorFrame owner) {
        super(owner, "Search in Project", false);
        this.editorFrame = owner;

        // Set up dialog layout.
        setLayout(new BorderLayout());
        setSize(400, 300);
        setLocationRelativeTo(owner);

        // Create the search field.
        searchField = new JTextField();
        add(searchField, BorderLayout.NORTH);

        // Create the list model and JList to display files.
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setCellRenderer(new FileListCellRenderer());
        add(new JScrollPane(fileList), BorderLayout.CENTER);

        // Load the prebuilt index and create IndexedFileEntry objects.
        loadAllFiles();
        // Do an initial filtering to populate the list.
        triggerFiltering();

        // Listen to document changes in the search field.
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleFiltering();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleFiltering();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleFiltering();
            }
        });

        // Open file on Enter in the search field.
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openSelectedFile();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    // Move focus to the file list.
                    fileList.requestFocusInWindow();
                    fileList.setSelectedIndex(0);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        // Also open file when Enter is pressed in the file list.
        fileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openSelectedFile();
                }
            }
        });

        // Double-clicking a file opens it.
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedFile();
                }
            }
        });
    }

    /**
     * Loads the list of files from the prebuilt index and converts them to IndexedFileEntry objects.
     */
    private void loadAllFiles() {
        List<File> allFiles = com.github.an0nn30.jpad.ui.search.ProjectFileSearchIndex.getIndexedFiles();
        if (allFiles == null) {
            allFiles = new ArrayList<>();
        }
        indexedEntries = new ArrayList<>(allFiles.size());
        for (File f : allFiles) {
            indexedEntries.add(new IndexedFileEntry(f));
        }
    }

    /**
     * Schedules the filtering operation after a brief delay.
     */
    private void scheduleFiltering() {
        if (searchTimer != null && searchTimer.isRunning()) {
            searchTimer.restart();
        } else {
            searchTimer = new Timer(SEARCH_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchTimer.stop();
                    triggerFiltering();
                }
            });
            searchTimer.setRepeats(false);
            searchTimer.start();
        }
    }

    /**
     * Triggers filtering asynchronously.
     */
    private void triggerFiltering() {
        final String query = searchField.getText().toLowerCase().trim();
        lastSearchQuery = query;
        new SwingWorker<List<File>, Void>() {
            @Override
            protected List<File> doInBackground() {
                List<File> results = new ArrayList<>();
                if (query.isEmpty()) {
                    for (IndexedFileEntry entry : indexedEntries) {
                        results.add(entry.file);
                    }
                } else {
                    for (IndexedFileEntry entry : indexedEntries) {
                        if (entry.searchKey.contains(query)) {
                            results.add(entry.file);
                        }
                    }
                }
                return results;
            }

            @Override
            protected void done() {
                // If the query changed while filtering was in progress, ignore these results.
                if (!lastSearchQuery.equals(query)) {
                    return;
                }
                try {
                    final List<File> filteredFiles = get();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            listModel.clear();
                            for (File f : filteredFiles) {
                                listModel.addElement(f);
                            }
                            if (!listModel.isEmpty()) {
                                fileList.setSelectedIndex(0);
                            }
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Opens the file that is currently selected in the list.
     */
    private void openSelectedFile() {
        File selected = fileList.getSelectedValue();
        if (selected != null) {
            TextAreaTabManager textAreaTabManager = editorFrame.getTabManager();
            textAreaTabManager.openFile(selected);
        }
        dispose();
    }

    /**
     * Custom list cell renderer that displays file paths relative to the project root.
     */
    private class FileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                File file = (File) value;
                DirectoryTree directoryTree = editorFrame.getDirectoryTree();
                File root = directoryTree.getRootDirectory();
                String displayText;
                if (root != null) {
                    String rootPath = root.getAbsolutePath();
                    String filePath = file.getAbsolutePath();
                    if (filePath.startsWith(rootPath)) {
                        // Show path relative to the project root.
                        displayText = filePath.substring(rootPath.length() + 1);
                    } else {
                        displayText = filePath;
                    }
                } else {
                    displayText = file.getAbsolutePath();
                }
                setText(displayText);
            }
            return this;
        }
    }
}