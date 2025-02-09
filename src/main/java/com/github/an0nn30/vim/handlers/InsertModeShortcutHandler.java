package com.github.an0nn30.vim.handlers;

import com.github.an0nn30.ui.VimTextArea;
import java.awt.event.KeyEvent;

public class InsertModeShortcutHandler extends VimModeShortcutHandler {

    public InsertModeShortcutHandler(VimTextArea editor) {
        super(editor);
    }

    @Override
    public boolean handleKeyPressed(KeyEvent e) {
        // In INSERT mode, let the text area handle input normally.
        // You might return false so that the default processing occurs.
        return false;
    }
}