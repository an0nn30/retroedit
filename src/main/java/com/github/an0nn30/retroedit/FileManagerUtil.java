package com.github.an0nn30.retroedit;

import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.Editor;
import com.github.an0nn30.retroedit.ui.utils.FileUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

public class FileManagerUtil {
    public static void openFile(Editor editor) {
        Logger.getInstance().info(FileManagerUtil.class, "Trying to open file?");

        if (Settings.getInstance().getInterfaceTheme().equalsIgnoreCase("retro")) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int result = fileChooser.showOpenDialog(editor);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file.isDirectory()) {
                    editor.getDirectoryTree().setRootDirectory(file);
                    System.out.println("directory");
                } else {
                    editor.getTabManager().openFile(fileChooser.getSelectedFile());
                }
            }
        } else {
            File file = FileUtils.openFileDialog(editor);
            if (file != null) {
                editor.getTabManager().openFile(file);
            }
        }
    }
}
