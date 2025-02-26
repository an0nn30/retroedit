package com.github.an0nn30.retroedit.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.an0nn30.retroedit.event.EventRecord;
import com.github.an0nn30.retroedit.settings.Constants;
import com.github.an0nn30.retroedit.event.EventBus;
import com.github.an0nn30.retroedit.event.EventType;
import com.github.an0nn30.retroedit.ui.components.Panel;
import com.github.an0nn30.retroedit.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * StatusPanel represents the status bar at the bottom of the editor.
 * It displays UI controls such as a terminal toggle button and a file type selector.
 * This class extends the custom {@link Panel} class for a consistent layout.
 */
public class StatusPanel extends Panel {

    private final EditorFrame editorFrame;
    // Tracks the visibility of the status panel.
    private boolean visible = true;

    /**
     * Constructs a new StatusPanel associated with the given EditorFrame.
     *
     * @param editorFrame the parent editor frame.
     */
    public StatusPanel(EditorFrame editorFrame) {
        super();
        this.editorFrame = editorFrame;

        // Create a button to toggle the terminal view.
        JButton toggleTerminal = createToggleTerminalButton();

        // Create a left toolbar for the terminal toggle button.
        JToolBar leftToolBar = createToolBar();
        leftToolBar.add(toggleTerminal);

        // Create a right toolbar for the file type selection combo box.
        JToolBar rightToolBar = createToolBar();
        rightToolBar.add(createFileTypeComboBox());

        // Add the toolbars to the status panel.
        addComponent("toggleTerminalButton", leftToolBar, PanelPosition.LEFT);
        addComponent("fileTypeComboBox", rightToolBar, PanelPosition.RIGHT);
    }

    /**
     * Creates a toggle button for switching the terminal view.
     *
     * @return a JButton configured with a terminal icon and an action listener.
     */
    private JButton createToggleTerminalButton() {
        // Create a button using the terminal icon from the ThemeManager.
        JButton button = new JButton(new FlatSVGIcon(ThemeManager.retroThemeIcons.get("terminal"), 18, 18));
        // When clicked, the button toggles the terminal view in the editor.
        button.addActionListener(e -> editorFrame.toggleTerminalView());
        return button;
    }

    /**
     * Creates a JToolBar with a fixed height and no floatable or rollover behavior.
     *
     * @return a JToolBar configured for the status panel.
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(false);
        toolBar.setMargin(new Insets(0, 0, 0, 0));
        // Set a fixed height (24px) while allowing the width to adjust.
        toolBar.setPreferredSize(new Dimension(100, 24));
        return toolBar;
    }

    /**
     * Creates a combo box for selecting a file type.
     * <p>
     * The combo box is populated with keys from {@link Constants#supportedFileTypes}. When a file type is selected,
     * a {@link EventBus} event is published to update the syntax highlighting.
     * It also listens for syntax highlight events (from other components) to update its selection.
     * </p>
     *
     * @return a JComboBox with file type options.
     */
    private JComboBox<String> createFileTypeComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        // Populate the combo box with file type keys.
        for (Map.Entry<String, String> entry : Constants.supportedFileTypes.entrySet()) {
            comboBox.addItem(entry.getKey());
        }
        // Publish a syntax highlight change when a new file type is selected.
        comboBox.addActionListener(e ->
                EventBus.publish(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(),
                        Constants.supportedFileTypes.get(comboBox.getSelectedItem()), comboBox));

        // Subscribe to syntax highlight changes from other components.
        EventBus.subscribe(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(), (EventRecord<Object> eventRecord) -> {
            // Avoid processing events that originated from this combo box.
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

    /**
     * Toggles the visibility of the status panel.
     * <p>
     * When called, if the panel is visible, it will be hidden; if hidden, it will be shown.
     * </p>
     */
    public void toggle() {
        visible = !visible;
        this.setVisible(visible);
    }
}