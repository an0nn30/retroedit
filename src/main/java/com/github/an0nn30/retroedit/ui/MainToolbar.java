package com.github.an0nn30.retroedit.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.utils.FileManagerUtil;
import com.github.an0nn30.retroedit.ui.components.Button;
import com.github.an0nn30.retroedit.ui.components.Panel;
import com.github.an0nn30.retroedit.ui.components.TextArea;
import com.github.an0nn30.retroedit.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * MainToolbar is a custom toolbar component that extends Panel.
 * It provides buttons for file operations such as New, Open, Save, and Refresh,
 * as well as placeholders for navigation and execution controls.
 */
public class MainToolbar extends Panel {

    private final EditorFrame editorFrame;
    private Panel toolbarPanel;

    /**
     * Constructs a MainToolbar attached to the given EditorFrame.
     * It installs the Flat IntelliJ Look and Feel and adds the toolbar buttons.
     *
     * @param editorFrame the parent EditorFrame.
     */
    public MainToolbar(EditorFrame editorFrame) {
        super(editorFrame);
        this.editorFrame = editorFrame;
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
                FileManagerUtil.openFile(editorFrame));

        Button saveButton = createButton("save", "Save", commonInsets, e ->
                editorFrame.getTabManager().saveFile(false));

        Button refreshButton = createButton("refresh", "Refresh", commonInsets, e -> refreshActiveFile());

        // Create placeholder navigation and execution buttons.
        Button backButton = createButton("back", null, commonInsets, e -> {});
        backButton.setEnabled(false);
        Button forwardButton = createButton("forward", null, commonInsets, e -> {});
        forwardButton.setEnabled(false);
        JComboBox<String> selectConfiguration = new JComboBox<>();
        selectConfiguration.setEnabled(false);
        Button runButton = createButton("run", null, commonInsets, e -> {});
        runButton.setEnabled(false);
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