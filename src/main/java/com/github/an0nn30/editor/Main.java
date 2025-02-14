package com.github.an0nn30.editor;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.github.an0nn30.editor.ui.Editor;

public class Main {
    public static void main(String[] args) {
        System.setProperty( "apple.awt.application.appearance", "NSAppearanceNameDarkAqua" );
        FlatIntelliJLaf.install();
        new Editor().setVisible(true);
    }
}
