package com.github.an0nn30.jpad.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.an0nn30.jpad.launchers.LaunchConfigManager;
import com.github.an0nn30.jpad.launchers.LaunchConfiguration;
import com.github.an0nn30.jpad.settings.Settings;
import com.github.an0nn30.jpad.ui.components.Button;
import com.github.an0nn30.jpad.ui.components.Panel;
import com.github.an0nn30.jpad.ui.components.TextArea;
import com.github.an0nn30.jpad.ui.theme.ThemeManager;
import com.github.an0nn30.jpad.ui.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

/**
 * MainToolbar is a custom toolbar component that extends Panel.
 * It provides buttons for file operations as well as run execution controls.
 */
public class MainToolbar extends Panel {

    private final EditorFrame editorFrame;
    private Panel toolbarPanel;
    // Promote the configuration combo box to a field so it can be accessed later.
    private JComboBox<String> selectConfiguration;
    // Reference to the launch configuration manager.
    private final LaunchConfigManager launchConfigManager;

    /**
     * Constructs a MainToolbar attached to the given EditorFrame.
     *
     * @param editorFrame         the parent EditorFrame.
     * @param launchConfigManager the manager that handles run configurations.
     */
    public MainToolbar(EditorFrame editorFrame, LaunchConfigManager launchConfigManager) {
        super(editorFrame);
        this.editorFrame = editorFrame;
        this.launchConfigManager = launchConfigManager;
        addComponent(initToolBar(), PanelPosition.LEFT);
    }

    /**
     * Initializes the toolbar panel by creating and adding toolbar buttons.
     *
     * @return a Panel containing all toolbar components.
     */
    private Panel initToolBar() {
        // Common insets for most buttons.
        final Insets commonInsets = new Insets(1, 2, 1, 2);

        // Create file operation buttons.
        Button newButton = createButton("new", null, new Insets(0, 2, 0, 2), e ->
                editorFrame.getTabManager().addNewTab("Untitled", new TextArea(editorFrame)));

        Button openButton = createButton("open", "Open", commonInsets, e ->
                FileUtils.openFileOrDirectory(editorFrame));

        Button saveButton = createButton("save", "Save", commonInsets, e ->
                editorFrame.getTabManager().saveFile(false));

        Button refreshButton = createButton("refresh", "Refresh", commonInsets, e -> refreshActiveFile());

        // Create placeholder navigation buttons.
        Button backButton = createButton("back", null, commonInsets, e -> {});
        backButton.setEnabled(false);
        Button forwardButton = createButton("forward", null, commonInsets, e -> {});
        forwardButton.setEnabled(false);

        // Create the configuration selection combo box.
        selectConfiguration = new JComboBox<>();
        // (Later, populate this combo box with configurations loaded from the JSON file.)
        selectConfiguration.setEnabled(selectConfiguration.getItemCount() > 0);

        // Create run button with our custom action listener.
        Button runButton = createButton("run", null, commonInsets, e -> onRunButtonPressed());
        runButton.setEnabled(true);

        Button stopButton = createButton("stop", null, commonInsets, e -> {});
        stopButton.setEnabled(false);

        Button toggleTerminalButton = createButton("terminal", null, commonInsets, e -> editorFrame.toggleTerminalView());

        // Assemble the toolbar panel.
        toolbarPanel = new Panel(editorFrame, 0, 0);
        toolbarPanel.addComponent(newButton, PanelPosition.LEFT);
        toolbarPanel.addComponent(openButton, PanelPosition.LEFT);
        toolbarPanel.addComponent(saveButton, PanelPosition.LEFT);
        toolbarPanel.addComponent(refreshButton, PanelPosition.LEFT);
        toolbarPanel.addComponent(backButton, PanelPosition.LEFT);
        toolbarPanel.addComponent(forwardButton, PanelPosition.LEFT);
        toolbarPanel.addComponent(selectConfiguration, PanelPosition.LEFT);
        toolbarPanel.addComponent(runButton, PanelPosition.LEFT);
        toolbarPanel.addComponent(stopButton, PanelPosition.LEFT);
        toolbarPanel.addComponent(toggleTerminalButton, PanelPosition.LEFT);

        return toolbarPanel;
    }

    /**
     * Called when the run button is pressed.
     * If a configuration is selected in the combo box, it executes that saved configuration.
     * Otherwise, it pops up a dialog with built‑in configurations.
     */
    private void onRunButtonPressed() {
        if (selectConfiguration.getItemCount() > 0) {
            Object selected = selectConfiguration.getSelectedItem();
            if (selected != null) {
                String configName = selected.toString();
                Optional<LaunchConfiguration> configOpt = launchConfigManager.getConfigurationByName(configName);
                if (configOpt.isPresent()) {
                    configOpt.get().execute();
                } else {
                    JOptionPane.showMessageDialog(editorFrame,
                            "Run configuration \"" + configName + "\" not found.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(editorFrame,
                        "No run configuration selected.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // No saved configurations available; show built‑in configurations.
            showBuiltInConfigDialog();
        }
    }

    /**
     * Displays a modal dialog with a list of built‑in configurations.
     * The dialog steals focus and allows the user to select a configuration.
     */
    private void showBuiltInConfigDialog() {
        JDialog dialog = new JDialog(editorFrame, "Select Built‑in Configuration", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(editorFrame);

        // Retrieve default configuration names from the manager.
        java.util.List<String> builtInConfigs = launchConfigManager.getDefaultConfigNames();
        JList<String> configList = new JList<>(builtInConfigs.toArray(new String[0]));
        configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!builtInConfigs.isEmpty()) {
            configList.setSelectedIndex(0);
        }
        JScrollPane scrollPane = new JScrollPane(configList);

        JButton runBuiltInButton = new JButton("Run");
        runBuiltInButton.addActionListener(e -> {
            String selectedConfig = configList.getSelectedValue();
            if (selectedConfig != null) {
                launchConfigManager.executeDefaultConfiguration(selectedConfig);
            }
            dialog.dispose();
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(runBuiltInButton, BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    /**
     * Creates a toolbar button with the specified icon key, text, insets, and action listener.
     * If the text parameter is null, the button will display only the icon.
     *
     * @param iconKey  the key to retrieve the icon from ThemeManager.icons.
     * @param text     the text label to display on the button (can be null).
     * @param insets   the Insets to use for the button.
     * @param listener the ActionListener to attach to the button.
     * @return a new Button instance.
     */
    private Button createButton(String iconKey, String text, Insets insets, java.awt.event.ActionListener listener) {
        String interfaceTheme = Settings.getInstance().getInterfaceTheme();
        FlatSVGIcon icon = ThemeManager.getIconForAction(iconKey, 16, 16);
        Button button = new Button(icon, insets);
        if (text != null && interfaceTheme.equalsIgnoreCase("retro")) {
            button.setText(text);
        }
        button.addActionListener(listener);
        return button;
    }

    /**
     * Refreshes the active file in the currently selected tab by re-reading its content.
     */
    private void refreshActiveFile() {
        TextArea textArea = editorFrame.getTabManager().getActiveTextArea();
        if (textArea == null) return;
        File file = textArea.getActiveFile();
        if (file == null) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            textArea.read(reader, null);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(editorFrame,
                    "Error refreshing file",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}