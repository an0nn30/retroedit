package com.github.an0nn30.retroedit.ui.utils;

import javax.swing.*;
import java.awt.FileDialog;
import java.io.File;

/**
 * Utility class for file operations such as opening and saving files,
 * as well as managing a "currentFile" property on Swing components.
 */
public class FileUtils {

    /**
     * The client property key used to store the current file.
     */
    public static final String CURRENT_FILE_PROPERTY = "currentFile";

    /**
     * Opens a native file dialog for selecting a file.
     * <p>
     * This method uses {@link FileDialog} to present a modal "Open File" dialog.
     * If the user selects a file, a {@link File} object representing the selected file is returned.
     * Otherwise, it returns null.
     * </p>
     *
     * @param parent the parent {@link JFrame} for the dialog.
     * @return the selected file, or null if no file was selected.
     */
    public static File openFileDialog(JFrame parent) {
        // Create a native file dialog for opening files.
        FileDialog fileDialog = new FileDialog(parent, "Open File", FileDialog.LOAD);
        fileDialog.setVisible(true);

        // Retrieve the selected file's name and directory.
        String fileName = fileDialog.getFile();
        if (fileName != null) {
            return new File(fileDialog.getDirectory(), fileName);
        }
        return null;
    }

    /**
     * Opens a Swing-based file chooser dialog for saving a file.
     * <p>
     * This method uses {@link JFileChooser} to allow the user to select a location and file name for saving.
     * If a file is selected, it returns a {@link File} object; otherwise, it returns null.
     * </p>
     *
     * @param parent the parent {@link JFrame} for the dialog.
     * @return the file selected for saving, or null if the operation was canceled.
     */
    public static File saveFileDialog(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Retrieves the "currentFile" stored as a client property of the given text component.
     * <p>
     * This method casts the provided component to {@link JComponent} and attempts to retrieve the
     * property defined by {@link #CURRENT_FILE_PROPERTY}. If the component is not a {@link JComponent},
     * or the property is not set, it returns null.
     * </p>
     *
     * @param textComponent the component from which to retrieve the current file.
     * @return the current file, or null if not set.
     */
    public static File getCurrentFile(Object textComponent) {
        if (textComponent instanceof JComponent) {
            return (File) ((JComponent) textComponent).getClientProperty(CURRENT_FILE_PROPERTY);
        }
        return null;
    }

    /**
     * Sets the "currentFile" as a client property on the given text component.
     * <p>
     * This method casts the provided component to {@link JComponent} and sets the
     * {@link #CURRENT_FILE_PROPERTY} to the specified file.
     * </p>
     *
     * @param textComponent the component on which to set the current file.
     * @param file          the file to set as current.
     */
    public static void setCurrentFile(Object textComponent, File file) {
        if (textComponent instanceof JComponent) {
            ((JComponent) textComponent).putClientProperty(CURRENT_FILE_PROPERTY, file);
        }
    }
}