package com.github.an0nn30.ui;

import javax.swing.*;
import java.awt.*;

public class EditorStatusBar {
    private final JPanel panel;
    private final JLabel statusLabel;

    public EditorStatusBar() {
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