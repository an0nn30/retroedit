package com.github.an0nn30.retroedit.ui.theme;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.EditorFrame;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ThemeManager is a utility class responsible for managing UI themes and icon resources.
 * It provides methods to configure the main window's look-and-feel and update the interface theme.
 */
public class ThemeManager {

    /**
     * A mapping of icon names to their resource paths.
     */
    public static final Map<String, String> icons;

    static {
        Map<String, String> tempIcons = new HashMap<>();
        tempIcons.put("open", "tango/scalable/actions/document-open.svg");
        tempIcons.put("new", "tango/scalable/actions/document-new.svg");
        tempIcons.put("save", "tango/scalable/actions/document-save.svg");
        tempIcons.put("back", "tango/scalable/actions/go-previous.svg");
        tempIcons.put("forward", "tango/scalable/actions/go-next.svg");
        tempIcons.put("run", "tango/scalable/actions/media-playback-start.svg");
        tempIcons.put("stop", "tango/scalable/actions/media-playback-stop.svg");
        tempIcons.put("refresh", "tango/scalable/actions/view-refresh.svg");
        tempIcons.put("terminal", "tango/scalable/apps/utilities-terminal.svg");
        tempIcons.put("file", "tango/scalable/mimetypes/text-x-generic.svg");
        tempIcons.put("tab-close", "tango/scalable/actions/closeActive.svg");
        icons = Collections.unmodifiableMap(tempIcons);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private ThemeManager() {
    }

    /**
     * Configures the look-and-feel for the main application window.
     * This implementation sets the cross-platform look-and-feel.
     *
     * @param frame the EditorFrame to configure.
     */
    public static void setupWindowFrame(EditorFrame frame) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            frame.setUndecorated(true);
            frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        } catch (Exception e) {
            Logger.getInstance().error(ThemeManager.class, "Failed to set LookAndFeel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the interface theme for the specified JFrame.
     * If an event record is provided, its string representation is used;
     * otherwise, the theme is retrieved from the application Settings.
     * Currently, only the "retro" theme is supported, which applies the cross-platform look-and-feel.
     *
     * @param frame       the JFrame whose theme should be updated.
     * @param eventRecord an optional event record containing the new theme.
     */
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