package com.github.an0nn30.retroedit;

import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.Editor;
import com.github.an0nn30.retroedit.ui.utils.FileUtils;

import javax.swing.*;
import java.io.File;

public class FileManagerUtil {
    public static void openFile(Editor editor) {
        Logger.getInstance().info(FileManagerUtil.class, "Trying to open file?");

        if (Settings.getInstance().getInterfaceTheme().equalsIgnoreCase("retro")) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(editor);
            if (result == JFileChooser.APPROVE_OPTION) {
                editor.getTabManager().openFile(fileChooser.getSelectedFile());
            }
        } else {
            File file = FileUtils.openFileDialog(editor);
            if (file != null) {
                editor.getTabManager().openFile(file);
            }
        }
    }
}
