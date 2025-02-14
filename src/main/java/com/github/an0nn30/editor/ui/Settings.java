package com.github.an0nn30.editor.ui;

import java.awt.*;

public class Settings {

    private static Color backgroundColor = Color.WHITE;
    private static int fontSize = 12;

    public static void initialize() {
        // TODO: Load settings from a configuration file or user preferences.
    }

    public static Color getBackgroundColor() {
        return backgroundColor;
    }

    public static int getFontSize() {
        return fontSize;
    }

    public static void setFontSize(int newSize) {
        fontSize = newSize;
        // Optionally notify other components that the font size has changed.
    }
}
