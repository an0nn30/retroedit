package com.github.an0nn30.jpad.ui;

import com.github.an0nn30.jpad.ui.components.Terminal;
import com.jediterm.terminal.ui.JediTermWidget;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class TerminalTabManager extends BaseTabManager<JediTermWidget> {

    private final EditorFrame editorFrame;
    private Map<String, JediTermWidget> terminalsMap = new HashMap<>();

    public TerminalTabManager(EditorFrame editorFrame) {
        super(SwingConstants.TOP);
        this.editorFrame = editorFrame;
        createNewTerminal("base");
    }

    public void createNewTerminal(String title) {
        JediTermWidget newTerminal = Terminal.createTerminalWidget(editorFrame);
        addComponentTab(title, newTerminal);
        terminalsMap.put(title, newTerminal);
    }

    public void addTerminal(String title, JediTermWidget terminal) {
        terminalsMap.put(title, terminal);
        addTab(title, terminal);
        terminal.requestFocus();
    }

    public void setSelectedTabByTitle(String title) {
        for (int i = 0; i < getTabCount(); i++) {
            if (getTitleAt(i).equals(title)) {
                setSelectedIndex(i);
                return;
            }
        }
    }

    public void removeTerminal(String title) {
        // Find the tab index with the matching title.
        for (int i = 0; i < getTabCount(); i++) {
            if (getTitleAt(i).equals(title)) {
                // Remove the tab from the tabbed pane.
                removeTabAt(i);
                // Remove the terminal from the map.
                terminalsMap.remove(title);
                return;
            }
        }
        System.err.println("No terminal tab found with title: " + title);
    }



    /**
     * Returns the active terminal. In this simple case, it’s the same as the active component.
     */
    public JediTermWidget getActiveTerminal() {
        return getActiveComponent();
    }

    public JediTermWidget getTerminal(String title) {
        return terminalsMap.get(title);
    }

}