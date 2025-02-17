package com.github.an0nn30.retroedit.ui.utils;

import javax.swing.*;
import java.awt.FileDialog;
import java.io.File;

public class FileUtils {
    public static File openFileDialog(JFrame parent) {
        FileDialog fileDialog = new FileDialog(parent, "Open File", FileDialog.LOAD);
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName != null) {
            return new File(fileDialog.getDirectory(), fileName);
        }
        return null;
    }

    public static File saveFileDialog(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public static File getCurrentFile(Object textComponent) {
        if (textComponent instanceof JComponent) {
            return (File) ((JComponent) textComponent).getClientProperty("currentFile");
        }
        return null;
    }

    public static void setCurrentFile(Object textComponent, File file) {
        if (textComponent instanceof JComponent) {
            ((JComponent) textComponent).putClientProperty("currentFile", file);
        }
    }
}