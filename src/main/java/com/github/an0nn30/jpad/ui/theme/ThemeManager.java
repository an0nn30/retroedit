package com.github.an0nn30.jpad.ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.an0nn30.jpad.logging.Logger;
import com.github.an0nn30.jpad.settings.Settings;
import com.github.an0nn30.jpad.ui.EditorFrame;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ThemeManager is a utility class responsible for managing UI themes and icon resources.
 * It provides methods to configure the main window's look-and-feel and update the interface theme.
 */
public class ThemeManager {

    /**
     * A mapping of icon names to their resource paths.
     */
    public static final Map<String, String> retroThemeIcons;
    public static final Map<String, String> defaultThemeIcons;

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
        tempIcons.put("java-file", "icons/java.svg");
        tempIcons.put("folder", "icons/folder.svg");
        tempIcons.put("empty-type", "icons/any_type.svg");
        tempIcons.put("xml-file", "icons/xml.svg");
        retroThemeIcons = Collections.unmodifiableMap(tempIcons);
        tempIcons.clear();

        tempIcons.put("open", "icons/menu-open_dark.svg");
        tempIcons.put("new", "icons/edit_dark.svg");
        tempIcons.put("save", "icons/menu-saveall_dark.svg");
        tempIcons.put("back", "icons/back.svg");
        tempIcons.put("forward", "icons/forward.svg");
        tempIcons.put("run", "icons/execute_dark.svg");
        tempIcons.put("stop", "icons/suspend.svg");
        tempIcons.put("terminal", "icons/OpenTerminal_13x13_dark.svg");
        tempIcons.put("refresh", "icons/refresh.svg");
        tempIcons.put("file", "icons/any_type.svg");
        tempIcons.put("tab-close", "icons/close_dark.svg");
        tempIcons.put("java-file", "icons/java.svg");
        tempIcons.put("folder", "icons/folder.svg");
        tempIcons.put("empty-type", "icons/any_type.svg");
        tempIcons.put("xml-file", "icons/xml.svg");
        defaultThemeIcons = Collections.unmodifiableMap(tempIcons);
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
    public static void setupWindowFrame(EditorFrame frame, String theme) {
        try {
            switch (theme) {
                case "Light":
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    break;
                case "Dark":
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;
                case "Retro":
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    break;
                default:
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }

            SwingUtilities.updateComponentTreeUI(frame);

        } catch (Exception e) {
            Logger.getInstance().error(ThemeManager.class, "Failed to set LookAndFeel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static FlatSVGIcon getIconForAction(String action, int width, int height) {
        String theme = Settings.getInstance().getInterfaceTheme();
        try {
            Map<String, String> icons = Objects.equals(theme, "Light") || Objects.equals(theme, "Dark") ? defaultThemeIcons : retroThemeIcons;
            return new FlatSVGIcon(icons.get(action), width, height);
        } catch (Exception e) {
            Logger.getInstance().error(ThemeManager.class, "Failed to load icon for action: " + action);
            return null;
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
    public static void updateInterfaceTheme(EditorFrame frame, Object eventRecord) {
        String theme = (eventRecord == null)
                ? Settings.getInstance().getInterfaceTheme()
                : eventRecord.toString();

        setupWindowFrame(frame, theme);
        SwingUtilities.updateComponentTreeUI(frame);
        frame.revalidate();
        frame.repaint();
    }
}