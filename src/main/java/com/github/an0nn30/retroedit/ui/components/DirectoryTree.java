package com.github.an0nn30.retroedit.ui.components;

import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.ui.Editor;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class DirectoryTree extends JTree {
    private DefaultTreeModel treeModel;
    private File rootDirectory;
    private boolean hideDotFiles = false;
    private Editor editor;

    public DirectoryTree(Editor editor) {
        this.editor = editor;
        // Initially, no directory is loaded.
        this.rootDirectory = null;
        // Create an empty tree with an empty root node.
        DefaultMutableTreeNode emptyRoot = new DefaultMutableTreeNode("");
        treeModel = new DefaultTreeModel(emptyRoot);
        setModel(treeModel);

        // Enable drag and drop
        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setTransferHandler(new FileTransferHandler());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Show context menu on popup trigger (platform dependent)
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
                // Handle double-click to open files
                if (e.getClickCount() == 2) {
                    TreePath path = getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        String filePath = getFilePathFromTreePath(path);
                        onFileDoubleClicked(filePath);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // On some platforms, popup trigger is on mouseReleased
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });
    }

    public void setHideDotFiles(boolean hideDotFiles) {
        this.hideDotFiles = hideDotFiles;
        refresh();
    }

    /**
     * Refreshes the tree view. If no rootDirectory is set, it clears the tree.
     */
    public void refresh() {
        if (rootDirectory == null) {
            // Clear the tree if no directory is selected.
            treeModel.setRoot(new DefaultMutableTreeNode(""));
            return;
        }
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootDirectory.getName());
        loadDirectory(rootNode, rootDirectory);
        treeModel.setRoot(rootNode);
    }

    /**
     * Sets the root directory for the tree and loads its content.
     *
     * @param rootDirectory The directory to load.
     */
    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        refresh();
    }

    /**
     * Recursively loads the directory structure into the tree.
     *
     * @param node      The parent node in the tree.
     * @param directory The file system directory to load.
     */
    public void loadDirectory(DefaultMutableTreeNode node, File directory) {
        if (!directory.isDirectory()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            if (hideDotFiles && file.getName().startsWith(".")) continue;
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file.getName());
            node.add(childNode);
            if (file.isDirectory()) {
                loadDirectory(childNode, file);
            }
        }
    }

    /**
     * Constructs the full file system path from the given TreePath.
     *
     * @param path The TreePath from the tree.
     * @return The full path as a string.
     */
    private String getFilePathFromTreePath(TreePath path) {
        if (rootDirectory == null) return "";
        StringBuilder fullPath = new StringBuilder(rootDirectory.getAbsolutePath());
        Object[] nodes = path.getPath();
        // Skip the first node since it is the root directory name.
        for (int i = 1; i < nodes.length; i++) {
            fullPath.append(File.separator).append(nodes[i].toString());
        }
        return fullPath.toString();
    }

    /**
     * Opens the file in the editor if it is a file.
     *
     * @param filePath The file system path.
     */
    private void onFileDoubleClicked(String filePath) {
        File file = new File(filePath);
        if (file.isFile()) {
            editor.getTabManager().openFile(file);
        }
    }

    /**
     * Shows a context menu for directory operations when right-clicking a directory.
     */
    private void showContextMenu(MouseEvent e) {
        TreePath path = getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }

        String filePath = getFilePathFromTreePath(path);
        File selectedFile = new File(filePath);
        // Only show the context menu if the selected node represents a directory.
        if (!selectedFile.isDirectory()) {
            return;
        }

        JPopupMenu contextMenu = new JPopupMenu();

        // New File option
        JMenuItem newFileItem = new JMenuItem("New File");
        newFileItem.addActionListener(ae -> {
            String fileName = JOptionPane.showInputDialog(DirectoryTree.this, "Enter new file name:");
            if (fileName != null && !fileName.trim().isEmpty()) {
                File newFile = new File(selectedFile, fileName);
                try {
                    boolean created = newFile.createNewFile();
                    if (!created) {
                        JOptionPane.showMessageDialog(DirectoryTree.this,
                                "File already exists or could not be created.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(DirectoryTree.this,
                            "Error creating file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                refresh();
            }
        });
        contextMenu.add(newFileItem);

        // New Directory option
        JMenuItem newDirItem = new JMenuItem("New Directory");
        newDirItem.addActionListener(ae -> {
            String dirName = JOptionPane.showInputDialog(DirectoryTree.this, "Enter new directory name:");
            if (dirName != null && !dirName.trim().isEmpty()) {
                File newDir = new File(selectedFile, dirName);
                if (!newDir.mkdir()) {
                    JOptionPane.showMessageDialog(DirectoryTree.this,
                            "Could not create directory.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                refresh();
            }
        });
        contextMenu.add(newDirItem);

        // Prevent deletion or renaming of the root directory.
        boolean isRoot = selectedFile.equals(rootDirectory);

        // Delete option
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setEnabled(!isRoot);
        deleteItem.addActionListener(ae -> {
            int confirm = JOptionPane.showConfirmDialog(DirectoryTree.this,
                    "Are you sure you want to delete this directory and its contents?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = deleteRecursively(selectedFile);
                if (!success) {
                    JOptionPane.showMessageDialog(DirectoryTree.this,
                            "Could not delete directory.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                refresh();
            }
        });
        contextMenu.add(deleteItem);

        // Rename option
        JMenuItem renameItem = new JMenuItem("Rename");
        renameItem.setEnabled(!isRoot);
        renameItem.addActionListener(ae -> {
            String newName = JOptionPane.showInputDialog(DirectoryTree.this,
                    "Enter new name:", selectedFile.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                File renamed = new File(selectedFile.getParent(), newName);
                if (!selectedFile.renameTo(renamed)) {
                    JOptionPane.showMessageDialog(DirectoryTree.this,
                            "Could not rename directory.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                refresh();
            }
        });
        contextMenu.add(renameItem);

        contextMenu.show(this, e.getX(), e.getY());
    }

    /**
     * Deletes a file or directory recursively.
     *
     * @param file The file or directory to delete.
     * @return true if deletion was successful, false otherwise.
     */
    private boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (!deleteRecursively(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    /**
     * A TransferHandler that supports dragging files or directories from the tree and
     * dropping them into other directories to move them.
     */
    private class FileTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            TreePath path = getSelectionPath();
            if (path != null) {
                String filePath = getFilePathFromTreePath(path);
                return new StringSelection(filePath);
            }
            return null;
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            // Only support drops and string data (the file path)
            if (!support.isDrop() || !support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }
            // Ensure drop location is a valid tree node representing a directory
            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            TreePath targetPath = dropLocation.getPath();
            if (targetPath == null) {
                return false;
            }
            String targetFilePath = getFilePathFromTreePath(targetPath);
            File targetFile = new File(targetFilePath);
            return targetFile.isDirectory();
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            TreePath targetPath = dropLocation.getPath();
            String targetDirPath = getFilePathFromTreePath(targetPath);
            File targetDir = new File(targetDirPath);

            Transferable t = support.getTransferable();
            try {
                String sourceFilePath = (String) t.getTransferData(DataFlavor.stringFlavor);
                File sourceFile = new File(sourceFilePath);

                // Prevent dropping into itself or one of its descendants
                if (targetDir.equals(sourceFile) || isChildOf(sourceFile, targetDir)) {
                    return false;
                }

                File destFile = new File(targetDir, sourceFile.getName());
                if (destFile.exists()) {
                    JOptionPane.showMessageDialog(DirectoryTree.this,
                            "A file or directory with that name already exists in the target location.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                boolean success = sourceFile.renameTo(destFile);
                if (!success) {
                    JOptionPane.showMessageDialog(DirectoryTree.this,
                            "Could not move the file/directory.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                refresh();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        /**
         * Helper method to check if 'child' is a descendant of 'parent'
         */
        private boolean isChildOf(File parent, File child) {
            try {
                File parentCanonical = parent.getCanonicalFile();
                File childCanonical = child.getCanonicalFile();
                while (childCanonical != null) {
                    if (childCanonical.equals(parentCanonical)) {
                        return true;
                    }
                    childCanonical = childCanonical.getParentFile();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }
}