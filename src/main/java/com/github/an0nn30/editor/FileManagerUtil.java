package com.github.an0nn30.editor;

import com.github.an0nn30.editor.ui.Editor;
import com.github.an0nn30.editor.ui.utils.FileUtils;

import java.io.File;

public class FileManagerUtil {
    public static void openFile(Editor editor) {
        File file = FileUtils.openFileDialog(editor);
        if (file != null) {
            editor.getTabManager().openFile(file);
        }
    }
}
