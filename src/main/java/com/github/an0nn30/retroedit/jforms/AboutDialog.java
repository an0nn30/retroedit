package com.github.an0nn30.retroedit.jforms;

import com.github.an0nn30.retroedit.utils.GitManifestInfo;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {

    public AboutDialog(Frame owner) {
        super(owner, "About", true);  // Modal dialog with the title "About"
        initUI();
    }

    private void initUI() {
        // Create a panel with vertical BoxLayout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

        // Create the "RetroDialog" label and center it
        JLabel titleLabel = new JLabel("RetroDialog");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create the "Version: " label and center it

        JLabel versionLabel = new JLabel("Version: ");
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        var version = GitManifestInfo.getGitBranch();
        var commit = GitManifestInfo.getGitCommit();
        if (version == null) {
            version = "Unknown Version";
        }
        if (commit == null) {
            commit = "Unknown Commit";
        }
        versionLabel.setText("Branch: " + version + ", Commit: " + commit);

        // Load the image from the resources folder (make sure retroedit.png is in src/main/resources)
        ImageIcon icon = new ImageIcon(getClass().getResource("/retroedit.png"));
        // Scale the image to 30x30 pixels
        Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to the panel with spacing in between
        panel.add(imageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(versionLabel);

        // Add the panel to the dialog
        getContentPane().add(panel, BorderLayout.CENTER);

        pack();  // Adjusts the dialog to fit the components
        setLocationRelativeTo(null);  // Centers the dialog on the screen
    }
}

