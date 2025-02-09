package com.github.an0nn30.vim.handlers;

import com.github.an0nn30.ui.VimTextArea;
import java.awt.event.KeyEvent;

public class VisualModeShortcutHandler extends VimModeShortcutHandler {

    public VisualModeShortcutHandler(VimTextArea editor) {
        super(editor);
    }

    @Override
    public boolean handleKeyPressed(KeyEvent e) {
        // Implement Visual mode–specific shortcuts here.
        // For now, return false so that unhandled keys are processed normally.
        return false;
    }
}