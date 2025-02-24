package com.github.an0nn30.retroedit;

import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.EditorFrame;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

/**
 * Main entry point for the Retro Edit application.
 */
public class Main {
    /**
     * The main method initializes settings, loads the custom font,
     * applies Linux scaling if available, installs the Look and Feel,
     * and displays the main EditorFrame.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        Settings.initialize();
        System.setProperty("apple.awt.application.appearance", "NSAppearanceNameAqua");

        // Detect and apply Linux scaling if running on Linux
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            double scaleFactor = getScaleFactor();
            System.setProperty("sun.java2d.uiScale", String.valueOf(scaleFactor));
            System.out.println("Detected Linux scale factor: " + scaleFactor);
        }

        // Uncomment to load and set a custom global font
//        loadAndSetInterfaceFont();

        SwingUtilities.invokeLater(() -> new EditorFrame().setVisible(true));
    }

    /**
     * Attempts to detect the system scale factor from multiple sources.
     *
     * @return the detected scale factor, or 1.0 if none found.
     */
    private static double getScaleFactor() {
        double xrdbScale = getScaleFactorFromXrdb();
        double gdkScale = getScaleFactorFromGDK();
        // Use the larger scale factor found (or 1.0 if neither is set)
        return Math.max(xrdbScale, gdkScale);
    }

    /**
     * Checks for a scaling factor via X resources (xrdb).
     *
     * @return the scale factor based on Xft.dpi (standard DPI is 96), or 1.0 if not found.
     */
    private static double getScaleFactorFromXrdb() {
        try {
            Process process = Runtime.getRuntime().exec("xrdb -query");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Xft.dpi:")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        double dpi = Double.parseDouble(parts[1]);
                        // A DPI of 96 is considered 1.0 scaling
                        return dpi / 96.0;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore exceptions and return default scale
        }
        return 1.0;
    }

    /**
     * Checks for a scaling factor via the GDK_SCALE environment variable.
     *
     * @return the scale factor from GDK_SCALE, or 1.0 if not set or invalid.
     */
    private static double getScaleFactorFromGDK() {
        String gdkScale = System.getenv("GDK_SCALE");
        if (gdkScale != null) {
            try {
                return Double.parseDouble(gdkScale);
            } catch (NumberFormatException e) {
                // Ignore invalid numbers
            }
        }
        return 1.0;
    }

    /**
     * Loads a custom font from resources and sets it as the default UI font.
     */
    private static void loadAndSetInterfaceFont() {
        try (InputStream is = Main.class.getResourceAsStream("/fonts/Consolas.ttf")) {
            if (is == null) {
                System.err.println("Font resource not found: /fonts/Consolas.ttf");
                return;
            }
            // Create the font and derive it to a desired size (e.g., 14pt)
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
            setGlobalUIFont(customFont);
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
