package com.github.an0nn30.retroedit.ui;

import com.github.an0nn30.retroedit.event.EventRecord;
import com.github.an0nn30.retroedit.settings.Constants;
import com.github.an0nn30.retroedit.event.EventBus;
import com.github.an0nn30.retroedit.event.EventType;
import com.github.an0nn30.retroedit.ui.components.Panel;

import javax.swing.*;
import java.util.Map;

public class StatusPanel extends Panel {

    public StatusPanel() {
        super();
        addComponent("fileTypeComboBox", createFileTypeComboBox(), PanelPosition.RIGHT);


//        EventBus.subscribe(EventType.THEME_CHANGED.name(), (Event<Object> event) -> updateTheme(event));
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
