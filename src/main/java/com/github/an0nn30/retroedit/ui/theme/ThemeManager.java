package com.github.an0nn30.retroedit.ui.theme;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import javax.swing.*;

public class ThemeManager {

    public static void setupWindowFrame(JFrame frame) {
        if (Settings.getSettings().getInterfaceTheme().equalsIgnoreCase("retro")) {
            frame.setUndecorated(true);
            frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        } else {
            FlatIntelliJLaf.setup();
        }
    }

    public static void updateInterfaceTheme(JFrame frame, Object eventRecord) {
        String theme = (eventRecord == null)
                ? Settings.getInstance().getInterfaceTheme()
                : eventRecord.toString();

        try {
            if (theme.equalsIgnoreCase("retro")) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        } catch (Exception e) {
            Logger.getInstance().error(ThemeManager.class, "Failed to set theme: " + e.getMessage());
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(frame);
        frame.revalidate();
        frame.repaint();
    }
}

