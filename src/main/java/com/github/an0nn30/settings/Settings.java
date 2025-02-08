package com.github.an0nn30.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Settings {
    private int fontSize;
    private String fontName;
    private String theme;

    private static final String SETTINGS_FILE = "settings.json";

    // Default constructor with default settings.
    public Settings() {
        this.fontSize = 12;
        this.fontName = "Monospaced";
        this.theme = "Monokai";
    }

    // Getters and setters.
    public int getFontSize() {
        return fontSize;
    }
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    public String getFontName() {
        return fontName;
    }
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }
    public String getTheme() {
        return theme;
    }
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Loads the settings from a JSON file using Gson.
     * If the file doesn't exist or fails to load, default settings are returned.
     */
    public static Settings loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            return new Settings();
        }
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, Settings.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new Settings();
        }
    }

    /**
     * Saves the current settings to a JSON file using Gson with pretty printing.
     */
    public void saveSettings() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}