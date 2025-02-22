package com.github.an0nn30.retroedit;

import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.EditorFrame;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * Main entry point for the Retro Edit application.
 */
public class Main {
    /**
     * The main method initializes settings, loads the custom font,
     * installs the Look and Feel, and displays the main EditorFrame.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        Settings.initialize();

        System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");

//        loadAndSetInterfaceFont();

        new EditorFrame().setVisible(true);
    }

    /**
     * Loads the Ubuntu-RI.ttf font from the resources folder, registers it, and sets it as the global UI font.
     */
    private static void loadAndSetInterfaceFont() {
        try (InputStream is = Main.class.getResourceAsStream("/fonts/Consolas.ttf")) {
            if (is == null) {
                System.err.println("Font resource not found: /fonts/resources/Ubuntu-RI.ttf");
                return;
            }
            // Create the font and derive it to the desired size (e.g., 14pt)
            Font ubuntuFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
            // Register the font with the graphics environment
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(ubuntuFont);
            // Set the font as the default UI font for all Swing components
            setGlobalUIFont(ubuntuFont);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterates through all UIManager defaults and replaces fonts with the provided font.
     *
     * @param font the font to be set as the default for all Swing components.
     */
    private static void setGlobalUIFont(Font font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, font);
            }
        }
    }
}