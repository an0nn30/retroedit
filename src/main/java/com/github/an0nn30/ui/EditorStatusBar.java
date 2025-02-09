package com.github.an0nn30.ui;

import javax.swing.*;
import java.awt.*;

public class EditorStatusBar {
    private final JPanel panel;
    private final JLabel statusLabel;    // Left-side label (e.g., file type)
    private final JLabel vimModeLabel;   // Right-side label to display Vim mode

    public EditorStatusBar() {
        panel = new JPanel(new BorderLayout());

        // Left-side status label.
        statusLabel = new JLabel(" File Type: None ");
        statusLabel.setOpaque(true);
        panel.add(statusLabel, BorderLayout.WEST);

        // Right-side vim mode label.
        vimModeLabel = new JLabel(" NORMAL ");
        vimModeLabel.setOpaque(true);
        // Set the default background color for NORMAL mode (pastel green).
        vimModeLabel.setBackground(new Color(144, 238, 144));
        panel.add(vimModeLabel, BorderLayout.EAST);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setText(String text) {
        statusLabel.setText(text);
    }

    /**
     * Updates the Vim mode display on the status bar.
     *
     * @param mode The current mode, e.g., "NORMAL", "VISUAL", or "INSERT".
     *             The label text is updated and the background is set to a corresponding pastel color.
     */
    public void setVimMode(String mode) {
        // Update the label text.
        vimModeLabel.setText(" " + mode + " ");

        // Update the background color based on the mode.
        switch (mode.toUpperCase()) {
            case "NORMAL":
                // Pastel green
                vimModeLabel.setBackground(new Color(144, 238, 144));
                break;
            case "VISUAL":
                // Light pastel blue
                vimModeLabel.setBackground(new Color(173, 216, 230));
                break;
            case "INSERT":
                // Light pastel yellow
                vimModeLabel.setBackground(new Color(255, 255, 224));
                break;
            default:
                // Use the panel's background color if unknown.
                vimModeLabel.setBackground(panel.getBackground());
                break;
        }
    }
}