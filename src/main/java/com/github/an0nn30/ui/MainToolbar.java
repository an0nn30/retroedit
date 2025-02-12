package com.github.an0nn30.ui;

import com.formdev.flatlaf.IntelliJTheme;

import javax.swing.*;
import java.awt.*;

public class MainToolbar extends BasePanel {
    public MainToolbar(EditorFrame frame) {
        super(frame);
        IntelliJTheme.setup(MainToolbar.class.getResourceAsStream("/DarkPurple.theme.json"));
        setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
    }
}
