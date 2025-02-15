package com.github.an0nn30.editor;

import com.github.an0nn30.editor.logging.Logger;
import com.github.an0nn30.editor.ui.Editor;
import com.github.an0nn30.editor.ui.utils.FileUtils;

import java.io.File;

public class FileManagerUtil {
    public static void openFile(Editor editor) {
        Logger.getInstance().info(FileManagerUtil.class, "Trying to open file?");

        File file = FileUtils.openFileDialog(editor);
        if (file != null) {
            editor.getTabManager().openFile(file);
        }
    }
}
