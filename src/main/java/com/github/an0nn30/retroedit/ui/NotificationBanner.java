package com.github.an0nn30.retroedit.ui;

import javax.swing.*;
import java.awt.*;

public class NotificationBanner extends JPanel {
    private Editor editor;
    public NotificationBanner(Editor editor) {
        this.editor = editor;
        this.setBackground(Color.YELLOW);


        this.add(new JLabel("Alert"));
        this.setVisible(true);
    }
}
