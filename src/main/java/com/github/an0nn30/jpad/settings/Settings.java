package com.github.an0nn30.jpad.settings;

import com.github.an0nn30.jpad.event.EventBus;
import com.github.an0nn30.jpad.event.EventType;
import com.google.gson.*;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Singleton class for managing application settings.
 * The settings are loaded from a JSON file (settings.json) and saved back when modified.
 */
public class Settings {
    private static Settings settings;

    private String interfaceTheme = "retro"; // Default theme can be adjusted.
    private int editorFontSize = 12;
    private int interfaceFontSize = 12;
    private String editorFontFamily = "Monospaced";
    private String interfaceFontFamily = "Monospaced";
    private String logLevel = "DEBUG";

    private static final File SETTINGS_FILE = getSettingsFilePath("settings.json").toFile();

    /**
     * Initializes the Settings singleton instance by loading from settings.json.
     * If the file does not exist or some settings are missing, default values are used and the file is updated.
     */
    public static void initialize() {
        Gson gson = createGson();
        boolean updated = false;

        // Attempt to load settings from file.
        settings = readSettingsFromFile(gson);
        if (settings == null) {
            settings = new Settings();
            updated = true;
        }

        // Validate settings against defaults.
        Settings defaults = new Settings();
        updated |= validateSettings(settings, defaults);

        if (updated) {
            saveSettings(gson);
        }
    }

    /**
     * Returns the singleton instance of Settings.
     *
     * @return the Settings instance.
     */
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

    /**
     * Reads the settings from the specified file using the provided Gson instance.
     *
     * @param gson the Gson instance to use for deserialization.
     * @return a Settings instance loaded from file, or null if reading fails.
     */
    private static Settings readSettingsFromFile(Gson gson) {
        if (SETTINGS_FILE.exists()) {
            try (FileReader reader = new FileReader(SETTINGS_FILE)) {
                return gson.fromJson(reader, Settings.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Validates the loaded settings against the provided default values.
     * If any setting is null or missing, it is replaced by the default.
     *
     * @param s        the Settings instance to validate.
     * @param defaults the default Settings instance.
     * @return true if any setting was updated; false otherwise.
     */
    private static boolean validateSettings(Settings s, Settings defaults) {
        boolean updated = false;
        if (s.interfaceTheme == null) {
            s.interfaceTheme = defaults.interfaceTheme;
            updated = true;
        }
        if (s.editorFontFamily == null) {
            s.editorFontFamily = defaults.editorFontFamily;
            updated = true;
        }
        if (s.interfaceFontFamily == null) {
            s.interfaceFontFamily = defaults.interfaceFontFamily;
            updated = true;
        }
        // Additional validations can be added here if needed.
        return updated;
    }

    /**
     * Saves the current settings to the settings file using the provided Gson instance.
     *
     * @param gson the Gson instance to use for serialization.
     */
    private static void saveSettings(Gson gson) {
        try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(settings, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a Gson instance with custom (de)serializers for Color and pretty printing enabled.
     *
     * @return a configured Gson instance.
     */
    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Color.class, (JsonSerializer<Color>) (src, typeOfSrc, context) ->
                        // Save the color as a hex string (ignoring alpha)
                        new JsonPrimitive(String.format("#%06x", src.getRGB() & 0xFFFFFF)))
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

    // Setter methods publish events if the value changes and then save settings.

    /**
     * Sets the interface theme.
     *
     * @param theme the new theme.
     */
    public void setInterfaceTheme(String theme) {
        if (!this.interfaceTheme.equalsIgnoreCase(theme)) {
            EventBus.publish(EventType.THEME_CHANGED.name(), theme, null);
            this.interfaceTheme = theme;
            saveSettings();
        }
    }

    /**
     * Sets the editor font size.
     *
     * @param editorFontSize the new editor font size.
     */
    public void setEditorFontSize(int editorFontSize) {
        if (this.editorFontSize != editorFontSize) {
            EventBus.publish(EventType.FONT_SIZE_CHANGED.name(), editorFontSize, null);
            this.editorFontSize = editorFontSize;
            saveSettings();
        }
    }

    /**
     * Sets the editor font family.
     *
     * @param editorFontFamily the new editor font family.
     */
    public void setEditorFontFamily(String editorFontFamily) {
        if (!this.editorFontFamily.equalsIgnoreCase(editorFontFamily)) {
            EventBus.publish(EventType.FONT_FAMILY_CHANGED.name(), editorFontFamily, null);
            this.editorFontFamily = editorFontFamily;
            saveSettings();
        }
    }

    // Getter methods.

    /**
     * Returns the interface theme.
     *
     * @return the interface theme.
     */
    public String getInterfaceTheme() {
        return interfaceTheme;
    }

    /**
     * Returns the editor font size.
     *
     * @return the editor font size.
     */
    public int getEditorFontSize() {
        return editorFontSize;
    }

    /**
     * Returns the interface font size.
     *
     * @return the interface font size.
     */
    public int getInterfaceFontSize() {
        return interfaceFontSize;
    }

    /**
     * Returns the editor font family.
     *
     * @return the editor font family.
     */
    public String getEditorFontFamily() {
        return editorFontFamily;
    }

    /**
     * Returns the interface font family.
     *
     * @return the interface font family.
     */
    public String getInterfaceFontFamily() {
        return interfaceFontFamily;
    }

    /**
     * Returns the log level.
     *
     * @return the log level.
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * Returns the path to the settings file.
     * The configuration directory is determined based on the operating system.
     *
     * @param fileName the name of the settings file.
     * @return the Path to the settings file.
     */
    public static Path getSettingsFilePath(String fileName) {
        String os = System.getProperty("os.name").toLowerCase();
        Path configDir;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            // Fallback in case APPDATA is not set.
            if (appData == null) {
                appData = System.getProperty("user.home");
            }
            configDir = Paths.get(appData, "retroedit");
        } else if (os.contains("mac")) {
            String home = System.getProperty("user.home");
            configDir = Paths.get(home, "Library", "Application Support", "com.github.an0nn30.retroedit", "retroedit");
        } else { // Assume Linux or Unix-like system.
            String home = System.getProperty("user.home");
            configDir = Paths.get(home, ".config", "r");
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