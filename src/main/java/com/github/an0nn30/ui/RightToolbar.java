package com.github.an0nn30.ui;

import javax.swing.*;
import java.awt.*;

public class RightToolbar extends JPanel {
    public RightToolbar() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(createRunButton());

    }

    public JButton createRunButton() {
        Icon originalIcon = new ImageIcon(getClass().getResource("/run.png"));

        // If the icon is an ImageIcon, scale its image.
        if (originalIcon instanceof ImageIcon) {
            Image originalImage = ((ImageIcon) originalIcon).getImage();
            // Adjust width and height as desired
            int newWidth = 20;
            int newHeight = 20;
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            Icon scaledIcon = new ImageIcon(scaledImage);

            JButton runButton = new JButton(scaledIcon);
            runButton.setToolTipText("Run");

            // Remove border, focus, and content area fill to have a flat look by default.
            runButton.setBorderPainted(false);
            runButton.setFocusPainted(false);
            runButton.setContentAreaFilled(false);

            // Optionally set a background color to use when hovered.
            runButton.setBackground(Color.DARK_GRAY);

            // Add a mouse listener to toggle content area filling on hover.
            runButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    runButton.setContentAreaFilled(true);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    runButton.setContentAreaFilled(false);
                }
            });

            return runButton;
        } else {
            // Fallback: if scaling isn't supported, apply the same hover behavior.
            JButton runButton = new JButton(originalIcon);
            runButton.setBorderPainted(false);
            runButton.setFocusPainted(false);
            runButton.setContentAreaFilled(false);
            runButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    runButton.setContentAreaFilled(true);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    runButton.setContentAreaFilled(false);
                }
            });
            return runButton;
        }
    }

}
