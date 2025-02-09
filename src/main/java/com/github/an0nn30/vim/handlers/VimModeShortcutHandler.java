package com.github.an0nn30.vim.handlers;

import com.github.an0nn30.ui.VimTextArea;
import java.awt.event.KeyEvent;

public abstract class VimModeShortcutHandler {
    protected VimTextArea editor;

    public VimModeShortcutHandler(VimTextArea editor) {
        this.editor = editor;
    }

    /**
     * Handle the key pressed event for this mode.
     * @param e the KeyEvent to process.
     * @return true if the event was handled, false otherwise.
     */
    public abstract boolean handleKeyPressed(KeyEvent e);
}