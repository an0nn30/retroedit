package com.github.an0nn30.retroedit.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.an0nn30.retroedit.event.EventRecord;
import com.github.an0nn30.retroedit.settings.Constants;
import com.github.an0nn30.retroedit.event.EventBus;
import com.github.an0nn30.retroedit.event.EventType;
import com.github.an0nn30.retroedit.ui.components.Panel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class StatusPanel extends Panel {
    private EditorFrame editorFrame;
    private boolean visible = true;

    public StatusPanel(EditorFrame editorFrame) {
        super();
        this.editorFrame = editorFrame;

        JButton toggleTerminal = new JButton(new FlatSVGIcon("icons/command_dark.svg"));
        toggleTerminal.addActionListener(e -> editorFrame.toggleTerminal());

        // Left toolbar with reduced height
        JToolBar leftToolBar = createToolBar();
        leftToolBar.add(toggleTerminal);

        // Right toolbar with reduced height
        JToolBar rightToolBar = createToolBar();
        rightToolBar.add(createFileTypeComboBox());

        // Add toolbars to the panel
        addComponent("toggleTerminalButton", leftToolBar, PanelPosition.LEFT);
        addComponent("fileTypeComboBox", rightToolBar, PanelPosition.RIGHT);
    }

    /**
     * Creates a toolbar with reduced height.
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(false);
        toolBar.setMargin(new Insets(0, 0, 0, 0));

        // Reduce toolbar height
        toolBar.setPreferredSize(new Dimension(100, 24)); // Adjust width dynamically, height fixed to 24px

        return toolBar;
    }

    /**
     * Toggles the visibility of the status panel.
     *
     * @param visible true to show, false to hide
     */
    public void toggle() {
        visible = !visible;
        this.setVisible(visible);
    }

    private JComboBox<String> createFileTypeComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        for (Map.Entry<String, String> entry : Constants.supportedFileTypes.entrySet()) {
            comboBox.addItem(entry.getKey());
        }

        // When the user selects a file type, publish a SYNTAX_HIGHLIGHT_CHANGED event.
        comboBox.addActionListener(e -> EventBus.publish(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(),
                Constants.supportedFileTypes.get(comboBox.getSelectedItem()), comboBox));

        // Subscribe to syntax highlight events (if coming from other components) to update the combo box.
        EventBus.subscribe(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(), (EventRecord<Object> eventRecord) -> {
            if (!eventRecord.source().equals(comboBox)) {
                for (Map.Entry<String, String> entry : Constants.supportedFileTypes.entrySet()) {
                    if (entry.getValue().equals(eventRecord.data())) {
                        comboBox.setSelectedItem(entry.getKey());
                    }
                }
            }
        });
        return comboBox;
    }
}