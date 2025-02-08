package com.github.an0nn30;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.an0nn30.ui.EditorFrame;
import com.github.an0nn30.ui.EditorTabManager;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            EditorFrame frame = new EditorFrame();

            // If a file argument is provided, try to open that file.
            if (args.length > 0) {
                File file = new File(args[0]);
                if (file.exists() && file.isFile()) {
                    EditorTabManager tabManager = frame.getTabManager();
                    tabManager.openFile(file);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "File not found: " + args[0],
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            frame.setVisible(true);
        });
    }
}
