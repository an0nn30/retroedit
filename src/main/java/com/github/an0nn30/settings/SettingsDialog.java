package com.github.an0nn30.settings;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {
    private final Settings settings;
    private JSpinner fontSizeSpinner;
    private JTextField fontNameField;
    private JComboBox<String> themeCombo;

    public SettingsDialog(Frame owner, Settings settings) {
        super(owner, "Settings", true);
        this.settings = settings;
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        panel.add(new JLabel("Font Size:"));
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(settings.getFontSize(), 8, 72, 1));
        panel.add(fontSizeSpinner);

        panel.add(new JLabel("Font Name:"));
        fontNameField = new JTextField(settings.getFontName());
        panel.add(fontNameField);

        panel.add(new JLabel("Theme:"));
        themeCombo = new JComboBox<>(new String[]{"Default", "Dark", "Eclipse", "Monokai"});
        themeCombo.setSelectedItem(settings.getTheme());
        panel.add(themeCombo);

        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        settings.setFontSize((Integer) fontSizeSpinner.getValue());
        settings.setFontName(fontNameField.getText());
        settings.setTheme((String) themeCombo.getSelectedItem());
        settings.saveSettings();
        dispose();
    }
}