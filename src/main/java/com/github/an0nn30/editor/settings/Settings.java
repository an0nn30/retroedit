package com.github.an0nn30.editor.settings;

import com.github.an0nn30.editor.event.EventBus;
import com.github.an0nn30.editor.event.EventType;
import com.google.gson.*;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Settings {
    private static Settings settings;
    private String interfaceTheme = "Light"; // you can adjust the default theme
    private int editorFontSize = 12;
    private int interfaceFontSize = 12;
    private String editorFontFamily = "Monospaced";
    private String interfaceFontFamily = "Monospaced";
    private String logLevel = "DEBUG";
    private static final File SETTINGS_FILE = getSettingsFilePath("settings.json").toFile();

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
    public void setInterfaceTheme(String theme) {
        if (!this.interfaceTheme.equalsIgnoreCase(theme)) {
            EventBus.publish(EventType.THEME_CHANGED.name(), theme, null);
        }
        this.interfaceTheme = theme;
        saveSettings();
    }
    public void setEditorFontSize(int editorFontSize) {
        if (this.editorFontSize != editorFontSize) {
            EventBus.publish(EventType.FONT_SIZE_CHANGED.name(), editorFontSize, null);
        }
        this.editorFontSize = editorFontSize;
        saveSettings();
    }

    public void setEditorFontFamily(String editorFontFamily) {
        if (!this.editorFontFamily.equalsIgnoreCase(editorFontFamily)) {
            EventBus.publish(EventType.FONT_FAMILY_CHANGED.name(), editorFontFamily, null);
        }
        this.editorFontFamily = editorFontFamily;
        saveSettings();
    }

    public static Settings getSettings() {
        return settings;
    }

    public String getInterfaceTheme() {
        return interfaceTheme;
    }

    public int getEditorFontSize() {
        return editorFontSize;
    }

    public int getInterfaceFontSize() {
        return interfaceFontSize;
    }

    public String getEditorFontFamily() {
        return editorFontFamily;
    }

    public String getInterfaceFontFamily() {
        return interfaceFontFamily;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public static Path getSettingsFilePath(String fileName) {
        String os = System.getProperty("os.name").toLowerCase();
        Path configDir;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            // Fallback in case APPDATA is not set.
            if (appData == null) {
                appData = System.getProperty("user.home");
            }
            configDir = Paths.get(appData, "TheEditor");
        } else if (os.contains("mac")) {
            String home = System.getProperty("user.home");
            configDir = Paths.get(home, "Library", "Application Support", "com.github.an0nn30.the-editor","TheEditor");
        } else { // Assume Linux or Unix-like system
            String home = System.getProperty("user.home");
            configDir = Paths.get(home, ".config", "TheEditor");
        }

        // Ensure the directory exists (creates if necessary)
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return configDir.resolve(fileName);
    }
}