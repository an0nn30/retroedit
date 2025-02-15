package com.github.an0nn30.editor.settings;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.github.an0nn30.editor.event.EventBus;
import com.github.an0nn30.editor.event.EventType;
import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

public class Settings {
    private static Settings settings;


    @Getter
    @Setter
    private String interfaceTheme = "Light"; // you can adjust the default theme

//    @Getter
//    @Setter
//    private String editorColorScheme = "monokai";

    @Getter
    @Setter
    private int editorFontSize = 12;

    @Setter
    @Getter
    private int interfaceFontSize = 12;

    @Setter
    @Getter
    private String editorFontFamily = "Monospaced";

    @Setter
    @Getter
    private String interfaceFontFamily = "Monospaced";

    // In Settings.java, add:
    @Getter
    @Setter
    private String logLevel = "DEBUG";

    private static final File SETTINGS_FILE = new File("settings.json");

    /**
     * Initializes the Settings instance by loading from settings.json.
     * If the file does not exist or a setting is missing, it uses the default value
     * and writes out the updated settings.
     */
    public static void initialize() {
        Gson gson = createGson();

        boolean updated = false;
        if (SETTINGS_FILE.exists()) {
            try (FileReader reader = new FileReader(SETTINGS_FILE)) {
                settings = gson.fromJson(reader, Settings.class);
            } catch (IOException e) {
                e.printStackTrace();
                // If something goes wrong, use defaults.
                settings = new Settings();
                updated = true;
            }
        } else {
            settings = new Settings();
            updated = true;
        }

        // Create a default instance to compare against
        Settings defaults = new Settings();


        // Check for any null settings that might have been omitted from the file.
        if (settings.interfaceTheme == null) {
            settings.interfaceTheme = defaults.interfaceTheme;
            updated = true;
        }
        if (settings.editorFontFamily == null) {
            settings.editorFontFamily = defaults.editorFontFamily;
            updated = true;
        }
        if (settings.interfaceFontFamily == null) {
            settings.interfaceFontFamily = defaults.interfaceFontFamily;
            updated = true;
        }
//        if (settings.editorColorScheme == null) {
//            settings.editorColorScheme = defaults.editorColorScheme;
//            updated = true;
//        }
        // Note: Primitive types (like int) cannot be null so they keep their default values.

        if (updated) {
            saveSettings(gson);
        }
    }

    public static Settings getInstance() {
        if (settings == null) {
            initialize();
        }
        return settings;
    }

    /**
     * Saves the current settings to the settings file.
     */
    public static void saveSettings() {
        Gson gson = createGson();
        saveSettings(gson);
    }

    private static void saveSettings(Gson gson) {
        try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(settings, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a Gson instance with custom (de)serializers for Color.
     */
    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Color.class, (JsonSerializer<Color>) (src, typeOfSrc, context) -> {
                    // Save the color as a hex string (ignoring alpha)
                    return new JsonPrimitive(String.format("#%06x", src.getRGB() & 0xFFFFFF));
                })
                .registerTypeAdapter(Color.class, new JsonDeserializer<Color>() {
                    @Override
                    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                            throws JsonParseException {
                        return Color.decode(json.getAsString());
                    }
                })
                .setPrettyPrinting()
                .create();
    }

    // === Example of custom setters that trigger saving ===
    // If you want every change to be immediately saved, consider replacing Lombok's
    // generated setters with these custom implementations.

    public void setInterfaceTheme(String theme) {
        if (!this.interfaceTheme.equalsIgnoreCase(theme)) {
            EventBus.publish(EventType.THEME_CHANGED.name(), theme, null);
        }
        this.interfaceTheme = theme;
        saveSettings();
    }

//    public void setEditorColorScheme(String theme) {
//        if (!this.editorColorScheme.equalsIgnoreCase(theme)) {
//            EventBus.publish(EventType.EDITOR_THEME_CHANGED.name(), theme, null);
//        }
//        this.editorFontFamily = theme;
//        saveSettings();
//    }

    public void setEditorFontSize(int editorFontSize) {
        if (this.editorFontSize != editorFontSize) {
            EventBus.publish(EventType.FONT_SIZE_CHANGED.name(), this.editorFontSize, null);
        }
        this.editorFontSize = editorFontSize;
        saveSettings();
    }

    public void setEditorFontFamily(String editorFontFamily) {
        if (!this.editorFontFamily.equalsIgnoreCase(editorFontFamily)) {
            EventBus.publish(EventType.FONT_FAMILY_CHANGED.name(), this.editorFontFamily, null);
        }
        this.editorFontFamily = editorFontFamily;
        saveSettings();
    }


    public void setInterfaceFontSize(int interfaceFontSize) {
        this.interfaceFontSize = interfaceFontSize;
        saveSettings();
    }


    public void setInterfaceFontFamily(String interfaceFontFamily) {
        this.interfaceFontFamily = interfaceFontFamily;
        saveSettings();
    }

    ///  Utility Functions for Themes

//    public void setTheme(JComponent component) {
//        if (component != null) {
//            component.setBackground(getEditorThemeColor());
//            for (Component child : component.getComponents()) {
//                child.setBackground(getEditorThemeColor());
//            }
//        }
//    }
}