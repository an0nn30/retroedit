package com.github.an0nn30.jpad.ui.utils;

import com.github.an0nn30.jpad.logging.Logger;
import com.github.an0nn30.jpad.ui.EditorFrame;

import javax.swing.*;
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
     * Opens a file or directory using a JFileChooser.
     * <p>
     * If the selected file is a directory, it is set as the root directory
     * of the DirectoryTree in the editor frame. Otherwise, the file is opened in a new tab.
     * </p>
     *
     * @param editorFrame the EditorFrame from which the file chooser is invoked.
     */
    public static void openFileOrDirectory(EditorFrame editorFrame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = fileChooser.showOpenDialog(editorFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.isDirectory()) {
                editorFrame.getDirectoryTree().setRootDirectory(selectedFile);
                Logger.getInstance().info(FileUtils.class, "Directory selected: " + selectedFile.getAbsolutePath());
            } else {
                editorFrame.getTabManager().openFile(selectedFile);
            }
        }
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