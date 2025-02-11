package com.github.an0nn30.utils;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import java.io.IOException;

public class ThemeUtils {
    public static void applyTheme(RSyntaxTextArea textArea, String themeName) {

        String themeResource = switch (themeName) {
            case "Dark" -> "/DarkPurple.rsyntaxarea.xml";
            case "Eclipse" -> "/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml";
            case "Monokai" -> "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";
            default -> "/org/fife/ui/rsyntaxtextarea/themes/default.xml";
        };
        try {
            Theme theme = Theme.load(ThemeUtils.class.getResourceAsStream(themeResource));
            theme.apply(textArea);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void applyDefaultTheme(RSyntaxTextArea textArea) {
        // For example, we choose Monokai as the default theme.
        applyTheme(textArea, "Dark");
    }
}