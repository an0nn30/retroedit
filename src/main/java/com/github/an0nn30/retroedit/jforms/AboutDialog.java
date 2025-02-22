package com.github.an0nn30.retroedit.jforms;


import javax.swing.*;
import java.awt.*;

/**
 * A modal dialog that displays "About" information for the application.
 * It shows the application logo, title, and Git branch/commit information.
 */
public class AboutDialog extends JDialog {

    /**
     * Constructs an AboutDialog with the specified owner frame.
     *
     * @param owner the parent frame of the dialog.
     */
    public AboutDialog(Frame owner) {
        super(owner, "About", true);
        initUI();
    }

    /**
     * Initializes the user interface of the AboutDialog.
     * Sets up the layout, creates UI components, and adds them to the dialog.
     */
    private void initUI() {
        // Create a main panel with a vertical BoxLayout and padding.
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Use custom window decoration.
        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

        // Create UI components.
        JLabel imageLabel = createImageLabel();
        JLabel titleLabel = createCenteredLabel("RetroDialog");
//        JLabel versionLabel = createVersionLabel();

        // Add components to the panel with spacing between them.
        panel.add(imageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
//        panel.add(versionLabel);

        // Add the panel to the dialog's content pane.
        getContentPane().add(panel, BorderLayout.CENTER);

        pack();                     // Adjust the dialog to fit its components.
        setLocationRelativeTo(null); // Center the dialog on the screen.
    }

    /**
     * Creates a centered JLabel with the specified text.
     *
     * @param text the text to display.
     * @return a JLabel with center alignment.
     */
    private JLabel createCenteredLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Creates a version label displaying the Git branch and commit information.
     * If no information is available, it uses default placeholder text.
     *
     * @return a centered JLabel with version information.
     */
//    private JLabel createVersionLabel() {
//        String version = GitManifestInfo.getGitBranch();
//        String commit = GitManifestInfo.getGitCommit();
//        if (version == null) {
//            version = "Unknown Version";
//        }
//        if (commit == null) {
//            commit = "Unknown Commit";
//        }
//        String labelText = "Branch: " + version + ", Commit: " + commit;
//        return createCenteredLabel(labelText);
//    }

    /**
     * Creates an image label by loading and scaling the "retroedit.png" resource.
     * The image is scaled to 150x150 pixels using smooth scaling.
     *
     * @return a centered JLabel containing the scaled image.
     */
    private JLabel createImageLabel() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/retroedit.png"));
        // Scale the image to 150x150 pixels.
        Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return imageLabel;
    }
}