package com.github.an0nn30.ui;

import com.formdev.flatlaf.IntelliJTheme;

import javax.swing.*;
import java.awt.*;

public class EditorStatusBar {
    private final JPanel panel;
    private final JLabel statusLabel;

    public EditorStatusBar() {
        IntelliJTheme.setup(this.getClass().getResourceAsStream("/DarkPurple.theme.json"));
        panel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" File Type: None ");
        panel.add(statusLabel, BorderLayout.WEST);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setText(String text) {
        statusLabel.setText(text);
    }
}