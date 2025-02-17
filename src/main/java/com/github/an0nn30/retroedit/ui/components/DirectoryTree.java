package com.github.an0nn30.retroedit.ui.components;

import com.github.an0nn30.retroedit.ui.Editor;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class DirectoryTree extends JTree {
    private DefaultTreeModel treeModel;
    private File rootDirectory;
    private boolean hideDotFiles = false;
    private Editor editor;

    public DirectoryTree(String directoryPath, Editor editor) {
        this.editor = editor;
        this.rootDirectory = new File(directoryPath);
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootDirectory.getName());
        treeModel = new DefaultTreeModel(rootNode);
        setModel(treeModel);
        loadDirectory(rootNode, rootDirectory);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        String filePath = getFilePathFromTreePath(path);
                        onFileDoubleClicked(filePath);
                    }
                }
            }
        });
    }

    public void setHideDotFiles(boolean hideDotFiles) {
        this.hideDotFiles = hideDotFiles;
        refresh();
    }

    public void refresh() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootDirectory.getName());
        loadDirectory(rootNode, rootDirectory);
        treeModel.setRoot(rootNode);
    }

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        refresh();
    }

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

    private String getFilePathFromTreePath(TreePath path) {
        StringBuilder fullPath = new StringBuilder(rootDirectory.getAbsolutePath());
        Object[] nodes = path.getPath();
        for (int i = 1; i < nodes.length; i++) {
            fullPath.append(File.separator).append(nodes[i].toString());
        }
        return fullPath.toString();
    }

    private void onFileDoubleClicked(String filePath) {
        File file = new File(filePath);
        if (file.isFile()) {
            editor.getTabManager().openFile(file);
        }
    }
}