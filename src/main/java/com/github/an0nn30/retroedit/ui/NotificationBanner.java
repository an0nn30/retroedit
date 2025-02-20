package com.github.an0nn30.retroedit.ui;

import javax.swing.*;
import java.awt.*;

public class NotificationBanner extends JPanel {
    private EditorFrame editorFrame;
    public NotificationBanner(EditorFrame editorFrame) {
        this.editorFrame = editorFrame;
        this.setBackground(Color.YELLOW);


        this.add(new JLabel("Alert"));
        this.setVisible(true);
    }
}
