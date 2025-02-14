package com.github.an0nn30.editor.ui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.an0nn30.editor.FileManagerUtil;
import com.github.an0nn30.editor.ui.components.Button;
import com.github.an0nn30.editor.ui.components.Panel;
import com.github.an0nn30.editor.ui.components.TextArea;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainToolbar extends Panel {

    private final Editor editor;

    public MainToolbar(Editor editor) {
        super(editor);
        this.editor = editor;
        FlatIntelliJLaf.install();

        addComponent(initToolBar(), PanelPosition.LEFT);

        // A search toolbar on the right (for future expansion)
        JToolBar searchToolbar = new JToolBar();
        searchToolbar.add(new JButton(new FlatSVGIcon("icons/search.svg")));
        addComponent("searchToolbar", searchToolbar, PanelPosition.RIGHT);
    }

    private JToolBar initToolBar() {
        JToolBar toolBar = new JToolBar();

        // Open: call FileManagerUtil with the Editor reference.
        Button openButton = new Button(new FlatSVGIcon("icons/menu-open_dark.svg"), "openButton");
        openButton.addActionListener(e -> FileManagerUtil.openFile(editor));

        // Save: call the TabManager’s save method directly.
        JButton saveButton = new JButton(new FlatSVGIcon("icons/menu-saveall_dark.svg"));
        saveButton.addActionListener(e -> editor.getTabManager().saveFile(false));

        // Refresh: re-read the active file.
        JButton refreshButton = new JButton(new FlatSVGIcon("icons/refresh.svg"));
        refreshButton.addActionListener(e -> {
            TextArea textArea = editor.getTabManager().getActiveTextArea();
            File file = textArea.getActiveFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textArea.read(reader, null);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(editor,
                        "Error refreshing file",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Other buttons (back, forward, build, etc.) remain as placeholders.
        JButton backButton = new JButton(new FlatSVGIcon("icons/back.svg"));
        backButton.setEnabled(false);
        JButton forwardButton = new JButton(new FlatSVGIcon("icons/forward.svg"));
        forwardButton.setEnabled(false);
        JButton buildButton = new JButton(new FlatSVGIcon("icons/toolWindowBuild_dark.svg"));
        buildButton.setEnabled(false);
        JComboBox<String> selectConfiguration = new JComboBox<>();
        selectConfiguration.setEnabled(false);
        JButton runButton = new JButton(new FlatSVGIcon("icons/execute_dark.svg"));
        runButton.setEnabled(false);
        JButton debugButton = new JButton(new FlatSVGIcon("icons/attachDebugger_dark.svg"));
        debugButton.setEnabled(false);
        JButton stopButton = new JButton(new FlatSVGIcon("icons/suspend.svg"));
        stopButton.setEnabled(false);

        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.add(refreshButton);
        toolBar.addSeparator();
        toolBar.add(backButton);
        toolBar.add(forwardButton);
        toolBar.addSeparator();
        toolBar.add(buildButton);
        toolBar.add(selectConfiguration);
        toolBar.add(runButton);
        toolBar.add(debugButton);
        toolBar.add(stopButton);

        return toolBar;
    }
}
