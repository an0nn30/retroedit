package com.github.an0nn30.editor.ui.platform;

import javax.swing.*;

public class MacUtils {
    public MacUtils() {

    }

    public void setMacTitleBar(JFrame frame) {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            frame.getRootPane().putClientProperty("apple.laf.useScreenMenuBar", true);
            frame.getRootPane().putClientProperty("apple.awt.application.name", "Editor");
//            frame.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
//            frame.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);

        }

    }
}
