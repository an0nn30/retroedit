package com.github.an0nn30.retroedit;

import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.EditorFrame;
import com.github.an0nn30.retroedit.ui.utils.FileUtils;

import javax.swing.*;
import java.io.File;

public class FileManagerUtil {
    public static void openFile(EditorFrame editorFrame) {
        Logger.getInstance().info(FileManagerUtil.class, "Trying to open file?");

        if (Settings.getInstance().getInterfaceTheme().equalsIgnoreCase("retro")) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int result = fileChooser.showOpenDialog(editorFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file.isDirectory()) {
                    editorFrame.getDirectoryTree().setRootDirectory(file);
                    System.out.println("directory");
                } else {
                    editorFrame.getTabManager().openFile(fileChooser.getSelectedFile());
                }
            }
        } else {
            File file = FileUtils.openFileDialog(editorFrame);
            if (file != null) {
                editorFrame.getTabManager().openFile(file);
            }
        }
    }
}
