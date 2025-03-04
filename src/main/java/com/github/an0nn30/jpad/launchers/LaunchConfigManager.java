package com.github.an0nn30.jpad.launchers;

import com.github.an0nn30.jpad.launchers.java.JavaMavenDefaultConfiguration;
import com.github.an0nn30.jpad.ui.EditorFrame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LaunchConfigManager {
    private List<LaunchConfiguration> configurations;
    private final Path configPath;
    private final Gson gson;
    private static final String CONFIG_DIR = ".retroedit";
    private static final String CONFIG_FILE = "launch.json";
    private final EditorFrame editorFrame;

    // List of built‑in default configurations.
    private List<DefaultRunConfiguration> defaultConfigurations;

    public LaunchConfigManager(EditorFrame editorFrame) {
        this.editorFrame = editorFrame;
        this.configPath = Paths.get(CONFIG_DIR, CONFIG_FILE);
        this.configurations = new ArrayList<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.defaultConfigurations = new ArrayList<>();
        // Register built‑in default configurations.
        defaultConfigurations.add(new JavaMavenDefaultConfiguration(editorFrame));
    }

    /**
     * Loads the launch configurations from the JSON file.
     * If the file does not exist, a default configuration is created.
     */
    public void loadConfigurations() throws IOException {
        if (Files.exists(configPath)) {
            String json = new String(Files.readAllBytes(configPath), StandardCharsets.UTF_8);
            Type configListType = new TypeToken<List<LaunchConfiguration>>() {}.getType();
            configurations = gson.fromJson(json, configListType);
        } else {
            System.out.println("Configuration file not found. Creating a default one.");
            createDefaultConfiguration();
        }
    }

    /**
     * Saves the current configurations to the JSON file.
     */
    public void saveConfigurations() throws IOException {
        Files.createDirectories(configPath.getParent());
        String json = gson.toJson(configurations);
        Files.write(configPath, json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a default launch configuration and saves it.
     */
    private void createDefaultConfiguration() throws IOException {
        LaunchConfiguration defaultConfig = new LaunchConfiguration();
        defaultConfig.setName("Default");
        defaultConfig.setCommand("echo");
        defaultConfig.setArgs(Arrays.asList("Hello, RetroEdit!"));
        // Optionally, set default environment variables if needed.
        configurations.add(defaultConfig);
        saveConfigurations();
    }

    /**
     * Retrieves a launch configuration by name.
     *
     * @param name the name of the configuration
     * @return an Optional containing the configuration if found, otherwise empty
     */
    public Optional<LaunchConfiguration> getConfigurationByName(String name) {
        return configurations.stream()
                .filter(config -> config.getName().equals(name))
                .findFirst();
    }

    public List<LaunchConfiguration> getConfigurations() {
        return configurations;
    }

    /**
     * Returns a list of names for all built‑in default configurations.
     *
     * @return a list of default configuration names.
     */
    public List<String> getDefaultConfigNames() {
        List<String> names = new ArrayList<>();
        for (DefaultRunConfiguration config : defaultConfigurations) {
            names.add(config.getName());
        }
        return names;
    }

    /**
     * Executes the built‑in default configuration that matches the given name.
     *
     * @param name the name of the default configuration.
     */
    public void executeDefaultConfiguration(String name) {
        for (DefaultRunConfiguration config : defaultConfigurations) {
            if (config.getName().equals(name)) {
                config.execute();
                return;
            }
        }
        System.out.println("Default configuration with name \"" + name + "\" not found.");
    }
}