package com.github.an0nn30.vim.handlers;

import com.github.an0nn30.ui.VimTextArea;
import java.awt.event.KeyEvent;

public class NormalModeShortcutHandler extends VimModeShortcutHandler {

    public NormalModeShortcutHandler(VimTextArea editor) {
        super(editor);
    }

    @Override
    public boolean handleKeyPressed(KeyEvent e) {
        char keyChar = e.getKeyChar();
        // In READY mode you can add more shortcuts if needed.
        // For instance, cursor movements could be here as well.
        return switch (keyChar) {
            case 'h' -> {
                editor.moveLeft();
                yield true;
            }
            case 'j' -> {
                editor.moveDown();
                yield true;
            }
            case 'k' -> {
                editor.moveUp();
                yield true;
            }
            case 'l' -> {
                editor.moveRight();
                yield true;
            }
            default -> false;
        };
    }
}