package com.github.an0nn30.retroedit.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.an0nn30.retroedit.ui.EditorFrame;
import com.github.an0nn30.retroedit.ui.search.ProjectFileSearchIndex;
import com.github.an0nn30.retroedit.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * A JTree component that displays a directory structure.
 * It supports features like hiding dot-files, drag-and-drop for moving files/directories,
 * context menus for creating, deleting, and renaming files/directories, and double-click to open files.
 */
public class DirectoryTree extends JTree {

    private DefaultTreeModel treeModel;
    private File rootDirectory;
    private boolean hideDotFiles = false;
    private EditorFrame editorFrame;

    /**
     * Constructs a DirectoryTree associated with the given EditorFrame.
     *
     * @param editorFrame the parent EditorFrame.
     */
    public DirectoryTree(EditorFrame editorFrame) {
        this.editorFrame = editorFrame;
        this.rootDirectory = null; // No directory loaded initially.
        // Initialize with an empty root node.
        DefaultMutableTreeNode emptyRoot = new DefaultMutableTreeNode("");
        treeModel = new DefaultTreeModel(emptyRoot);
        setModel(treeModel);

        // Install custom cell renderer for adding icons to .java files and directories.
        setCellRenderer(new JavaFileTreeCellRenderer());

        // Enable drag-and-drop functionality.
        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setTransferHandler(new FileTransferHandler());

        // Add a mouse listener to handle context menu and double-click events.
        addMouseListener(createMouseListener());
    }

    /**
     * Sets whether files beginning with a dot (hidden files) should be hidden.
     *
     * @param hideDotFiles true to hide dot-files, false to show.
     */
    public void setHideDotFiles(boolean hideDotFiles) {
        this.hideDotFiles = hideDotFiles;
        refresh();
    }

    /**
     * Refreshes the tree view by reloading the directory structure.
     * If no root directory is set, the tree is cleared.
     */
    public void refresh() {
        if (rootDirectory == null) {
            treeModel.setRoot(new DefaultMutableTreeNode(""));
            return;
        }
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootDirectory);
        loadDirectory(rootNode, rootDirectory);
        treeModel.setRoot(rootNode);
    }

    /**
     * Sets the root directory to display in the tree and refreshes the view.
     * Also triggers asynchronous indexing of the project files.
     *
     * @param rootDirectory the directory to load.
     */
    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        refresh();
        ProjectFileSearchIndex.buildIndex(rootDirectory);
    }

    /**
     * Recursively loads the file system directory into the tree starting from the specified directory.
     *
     * @param parentNode the parent node in the tree.
     * @param directory  the directory to load.
     */
    public void loadDirectory(DefaultMutableTreeNode parentNode, File directory) {
        if (!directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            if (hideDotFiles && file.getName().startsWith(".")) {
                continue;
            }
            // Store the actual File object so that the renderer can decide on the icon.
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
            parentNode.add(childNode);
            if (file.isDirectory()) {
                loadDirectory(childNode, file);
            }
        }
    }

    /**
     * Constructs the full file system path from the provided TreePath.
     *
     * @param path the TreePath representing a node in the tree.
     * @return the full file system path as a string.
     */
    private String getFilePathFromTreePath(TreePath path) {
        if (rootDirectory == null) {
            return "";
        }
        StringBuilder fullPath = new StringBuilder(rootDirectory.getAbsolutePath());
        Object[] nodes = path.getPath();
        // Skip the first node as it represents the root directory.
        for (int i = 1; i < nodes.length; i++) {
            Object obj = ((DefaultMutableTreeNode) nodes[i]).getUserObject();
            if (obj instanceof File) {
                fullPath.append(File.separator).append(((File) obj).getName());
            } else {
                fullPath.append(File.separator).append(obj.toString());
            }
        }
        return fullPath.toString();
    }

    /**
     * Handles double-click events on tree nodes to open files in the editor.
     *
     * @param filePath the file system path of the double-clicked node.
     */
    private void onFileDoubleClicked(String filePath) {
        File file = new File(filePath);
        if (file.isFile()) {
            editorFrame.getTabManager().openFile(file);
        }
    }

    /**
     * Creates and returns a MouseAdapter to handle mouse events on the tree.
     *
     * @return the MouseAdapter.
     */
    private MouseAdapter createMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePopupTrigger(e);
                if (e.getClickCount() == 2) {
                    handleDoubleClick(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handlePopupTrigger(e);
            }
        };
    }

    /**
     * Checks if the mouse event is a popup trigger and shows the context menu if so.
     *
     * @param e the MouseEvent.
     */
    private void handlePopupTrigger(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showContextMenu(e);
        }
    }

    /**
     * Handles a double-click mouse event by opening the file (if applicable).
     *
     * @param e the MouseEvent.
     */
    private void handleDoubleClick(MouseEvent e) {
        TreePath path = getPathForLocation(e.getX(), e.getY());
        if (path != null) {
            String filePath = getFilePathFromTreePath(path);
            onFileDoubleClicked(filePath);
        }
    }

    /**
     * Displays a context menu with directory operations at the location of the mouse event.
     *
     * @param e the MouseEvent triggering the context menu.
     */
    private void showContextMenu(MouseEvent e) {
        TreePath path = getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        String filePath = getFilePathFromTreePath(path);
        File selectedFile = new File(filePath);
        if (!selectedFile.isDirectory()) {
            return;
        }
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.add(createMenuItem("New File", ae -> createNewFileInDirectory(selectedFile)));
        contextMenu.add(createMenuItem("New Directory", ae -> createNewDirectoryInDirectory(selectedFile)));
        boolean isRoot = selectedFile.equals(rootDirectory);
        JMenuItem deleteItem = createMenuItem("Delete", ae -> deleteDirectoryWithConfirmation(selectedFile));
        deleteItem.setEnabled(!isRoot);
        contextMenu.add(deleteItem);
        JMenuItem renameItem = createMenuItem("Rename", ae -> renameDirectory(selectedFile));
        renameItem.setEnabled(!isRoot);
        contextMenu.add(renameItem);
        contextMenu.show(this, e.getX(), e.getY());
    }

    /**
     * Creates a JMenuItem with the given text and action listener.
     *
     * @param text     the text for the menu item.
     * @param listener the action listener to attach.
     * @return the created JMenuItem.
     */
    private JMenuItem createMenuItem(String text, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        return item;
    }

    /**
     * Prompts the user for a file name and creates a new file in the specified directory.
     *
     * @param directory the directory in which to create the file.
     */
    private void createNewFileInDirectory(File directory) {
        String fileName = JOptionPane.showInputDialog(DirectoryTree.this, "Enter new file name:");
        if (fileName != null && !fileName.trim().isEmpty()) {
            File newFile = new File(directory, fileName);
            try {
                boolean created = newFile.createNewFile();
                if (!created) {
                    showError("File already exists or could not be created.");
                }
            } catch (IOException ex) {
                showError("Error creating file: " + ex.getMessage());
            }
            refresh();
        }
    }

    /**
     * Prompts the user for a directory name and creates a new directory in the specified directory.
     *
     * @param directory the directory in which to create the new directory.
     */
    private void createNewDirectoryInDirectory(File directory) {
        String dirName = JOptionPane.showInputDialog(DirectoryTree.this, "Enter new directory name:");
        if (dirName != null && !dirName.trim().isEmpty()) {
            File newDir = new File(directory, dirName);
            if (!newDir.mkdir()) {
                showError("Could not create directory.");
            }
            refresh();
        }
    }

    /**
     * Prompts the user for confirmation and deletes the specified directory (or file) recursively.
     *
     * @param file the file or directory to delete.
     */
    private void deleteDirectoryWithConfirmation(File file) {
        int confirm = JOptionPane.showConfirmDialog(DirectoryTree.this,
                "Are you sure you want to delete this directory and its contents?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = deleteRecursively(file);
            if (!success) {
                showError("Could not delete directory.");
            }
            refresh();
        }
    }

    /**
     * Prompts the user for a new name and renames the specified file or directory.
     *
     * @param file the file or directory to rename.
     */
    private void renameDirectory(File file) {
        String newName = JOptionPane.showInputDialog(DirectoryTree.this, "Enter new name:", file.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            File renamed = new File(file.getParent(), newName);
            if (!file.renameTo(renamed)) {
                showError("Could not rename directory.");
            }
            refresh();
        }
    }

    /**
     * Recursively deletes a file or directory.
     *
     * @param file the file or directory to delete.
     * @return true if deletion was successful; false otherwise.
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
     * Displays an error message dialog with the specified message.
     *
     * @param message the error message to display.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(DirectoryTree.this,
                message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /* ========================= Drag and Drop Support ========================= */

    /**
     * A TransferHandler implementation for handling drag-and-drop of files and directories
     * within the directory tree.
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
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop() || !support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }
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
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            TreePath targetPath = dropLocation.getPath();
            String targetDirPath = getFilePathFromTreePath(targetPath);
            File targetDir = new File(targetDirPath);

            Transferable transferable = support.getTransferable();
            try {
                String sourceFilePath = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                File sourceFile = new File(sourceFilePath);
                if (targetDir.equals(sourceFile) || isChildOf(sourceFile, targetDir)) {
                    return false;
                }
                File destFile = new File(targetDir, sourceFile.getName());
                if (destFile.exists()) {
                    showError("A file or directory with that name already exists in the target location.");
                    return false;
                }
                boolean success = sourceFile.renameTo(destFile);
                if (!success) {
                    showError("Could not move the file/directory.");
                    return false;
                }
                refresh();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

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

    /**
     * Returns the current root directory.
     *
     * @return the current root directory.
     */
    public File getRootDirectory() {
        return this.rootDirectory;
    }

    /**
     * Selects and expands the tree node corresponding to the specified file.
     *
     * @param file the file to select in the tree.
     */
    public void selectFile(File file) {
        if (rootDirectory == null) {
            return;
        }
        try {
            String rootPath = rootDirectory.getCanonicalPath();
            String filePath = file.getCanonicalPath();
            if (!filePath.startsWith(rootPath)) {
                return;
            }
            String relativePath = filePath.substring(rootPath.length());
            if (relativePath.startsWith(File.separator)) {
                relativePath = relativePath.substring(1);
            }
            String[] pathParts = relativePath.isEmpty() ? new String[0] : relativePath.split(Pattern.quote(File.separator));
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
            TreePath treePath = new TreePath(rootNode);
            treePath = findTreePath(rootNode, pathParts, 0);
            if (treePath != null) {
                setSelectionPath(treePath);
                scrollPathToVisible(treePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively searches for a TreePath matching the provided path parts.
     *
     * @param node      the current node to search.
     * @param pathParts an array of directory/file names representing the relative path.
     * @param index     the current index in the path parts.
     * @return the TreePath if found; otherwise, null.
     */
    private TreePath findTreePath(DefaultMutableTreeNode node, String[] pathParts, int index) {
        if (index >= pathParts.length) {
            return new TreePath(node.getPath());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            Object userObj = child.getUserObject();
            String nodeName;
            if (userObj instanceof File) {
                nodeName = ((File) userObj).getName();
            } else {
                nodeName = userObj.toString();
            }
            if (nodeName.equals(pathParts[index])) {
                TreePath path = findTreePath(child, pathParts, index + 1);
                if (path != null) {
                    return path;
                }
            }
        }
        return null;
    }

    /**
     * Custom TreeCellRenderer that prepends an icon for .java files and directories.
     * For all other file types, it uses the "empty-type" icon.
     */
    private class JavaFileTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObj = node.getUserObject();
                String label;
                if (userObj instanceof File) {
                    File f = (File) userObj;
                    label = f.getName();
                    if (f.isDirectory()) {
                        setIcon(new FlatSVGIcon(ThemeManager.retroThemeIcons.get("folder")));
                    } else if (label.toLowerCase().endsWith(".java")) {
                        setIcon(new FlatSVGIcon(ThemeManager.retroThemeIcons.get("java-file")));
                    } else if (label.toLowerCase().endsWith(".xml")) {
                        setIcon(new FlatSVGIcon(ThemeManager.retroThemeIcons.get("xml-file")));
                    } else {
                        setIcon(new FlatSVGIcon(ThemeManager.retroThemeIcons.get("empty-type")));
                    }
                } else {
                    label = userObj.toString();
                }
                setText(label);
            }
            return c;
        }
    }
}