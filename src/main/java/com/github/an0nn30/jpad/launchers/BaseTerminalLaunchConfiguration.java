package com.github.an0nn30.jpad.launchers;

import com.github.an0nn30.jpad.ui.EditorFrame;
import com.github.an0nn30.jpad.ui.TerminalTabManager;
import com.jediterm.terminal.ui.JediTermWidget;

/**
 * An abstract base class for launch configurations that need to run commands
 * in a terminal. It extracts common terminal handling logic so that subclasses
 * can simply build their command string and call runInTerminal().
 */
public abstract class BaseTerminalLaunchConfiguration implements DefaultRunConfiguration {

    protected final EditorFrame editorFrame;
    protected final TerminalLauncher terminalLauncher;

    public BaseTerminalLaunchConfiguration(EditorFrame editorFrame) {
        this.editorFrame = editorFrame;
        this.terminalLauncher = new TerminalLauncher();
    }

    /**
     * Runs the provided command string in a terminal.
     * If a terminal with the given title already exists in the TerminalTabManager,
     * that terminal is reused; otherwise, a new terminal is created and added.
     *
     * @param terminalTitle The title of the terminal tab.
     * @param commands      The command string to send (with newline characters as needed).
     */
    protected void runInTerminal(String terminalTitle, String commands) {
        TerminalTabManager terminalTabManager = editorFrame.getTerminalTabManager();
        // Check if a terminal with the given title already exists.
        JediTermWidget terminal = terminalTabManager.getTerminal(terminalTitle);
        if (terminal == null) {
            terminal = terminalLauncher.initTerminal();
            terminalTabManager.addTerminal(terminalTitle, terminal);
        } else {
            terminalTabManager.setSelectedTabByTitle(terminalTitle);
        }
        // Ensure the terminal view is visible.
        if (!editorFrame.getIsTerminalToggled()) {
            editorFrame.toggleTerminalView();
        }
        this.editorFrame.getTerminalTabManager().setSelectedTabByTitle(terminalTitle);
        terminalLauncher.sendCommand(commands);
        // TODO: Find a way to publish event when the commands are done.
    }
}
