package com.github.an0nn30.retroedit;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.Editor;

public class Main {
    public static void main(String[] args) {
        Settings.initialize();
        var settings = Settings.getInstance();
        switch (settings.getInterfaceTheme()) {
            case "Light":
                System.setProperty( "apple.awt.application.appearance", "NSAppearanceNameAqua" );
                break;
            case "Dark":
                System.setProperty( "apple.awt.application.appearance", "NSAppearanceNameDarkAqua" );
        }
        FlatIntelliJLaf.install();
        new Editor().setVisible(true);
    }
}
