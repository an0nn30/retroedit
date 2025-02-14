package com.github.an0nn30.editor.ui;

import com.github.an0nn30.editor.settings.Constants;
import com.github.an0nn30.editor.event.Event;
import com.github.an0nn30.editor.event.EventBus;
import com.github.an0nn30.editor.event.EventType;
import com.github.an0nn30.editor.ui.components.Panel;
import com.github.an0nn30.editor.ui.components.Panel.PanelPosition;
import javax.swing.*;
import java.util.Map;

public class StatusPanel extends Panel {

    public StatusPanel() {
        super();
        addComponent("fileTypeComboBox", createFileTypeComboBox(), PanelPosition.RIGHT);
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
        EventBus.subscribe(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(), (Event<Object> event) -> {
            if (!event.source().equals(comboBox)) {
                for (Map.Entry<String, String> entry : Constants.supportedFileTypes.entrySet()) {
                    if (entry.getValue().equals(event.data())) {
                        comboBox.setSelectedItem(entry.getKey());
                    }
                }
            }
        });
        return comboBox;
    }
}
