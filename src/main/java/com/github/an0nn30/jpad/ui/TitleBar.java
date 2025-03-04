package com.github.an0nn30.jpad.ui;

import javax.swing.*;
import java.awt.*;

public class TitleBar extends JPanel {

    public TitleBar(EditorFrame editorFrame, MainToolbar mainToolbar) {
        // Use a vertical BoxLayout so components are stacked
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Create an empty panel with a fixed height to sit behind the macOS titlebar.
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension(0, 30));  // fixed height of 30 pixels
        emptyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        emptyPanel.setMinimumSize(new Dimension(0, 30));
        emptyPanel.setOpaque(false); // make it transparent
        // Add the empty panel first then the MainToolbar
        add(emptyPanel);
        add(mainToolbar);
    }
}