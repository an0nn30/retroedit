package com.github.an0nn30.retroedit.ui.utils;

import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.EditorFrame;

import javax.swing.*;
import java.io.File;

/**
 * Utility class for file management operations within the editor.
 * Provides methods to open files or directories based on the current interface theme.
 */
public class FileManagerUtil {

    /**
     * Opens a file or directory for the given EditorFrame.
     * <p>
     * If the interface theme is set to "retro", a JFileChooser is used to select a file or directory.
     * If a directory is chosen, it is loaded into the DirectoryTree.
     * Otherwise, a custom file dialog is used via FileUtils.
     * </p>
     *
     * @param editorFrame the EditorFrame from which to open a file.
     */
    public static void openFile(EditorFrame editorFrame) {
        Logger.getInstance().info(FileManagerUtil.class, "Trying to open file?");
        openFileUsingFileChooser(editorFrame);
    }

    /**
     * Checks whether the current interface theme is "retro".
     *
     * @return true if the theme is "retro"; false otherwise.
     */
    private static boolean isRetroTheme() {
        return Settings.getInstance().getInterfaceTheme().equalsIgnoreCase("retro");
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
    private static void openFileUsingFileChooser(EditorFrame editorFrame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = fileChooser.showOpenDialog(editorFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.isDirectory()) {
                editorFrame.getDirectoryTree().setRootDirectory(selectedFile);
                Logger.getInstance().info(FileManagerUtil.class, "Directory selected: " + selectedFile.getAbsolutePath());
            } else {
                editorFrame.getTabManager().openFile(selectedFile);
            }
        }
    }

    /**
     * Opens a file using a custom file dialog provided by FileUtils.
     * If a file is selected, it is opened in a new tab.
     *
     * @param editorFrame the EditorFrame from which the file dialog is invoked.
     */
    private static void openFileUsingFileUtils(EditorFrame editorFrame) {
        File file = FileUtils.openFileDialog(editorFrame);
        if (file != null) {
            editorFrame.getTabManager().openFile(file);
        }
    }
}