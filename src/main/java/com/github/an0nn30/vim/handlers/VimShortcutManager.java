package com.github.an0nn30.vim.handlers;

import com.github.an0nn30.ui.VimTextArea;
import com.github.an0nn30.vim.VimModes;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class VimShortcutManager extends KeyAdapter {
    private final VimTextArea editor;
    private final Map<VimModes, VimModeShortcutHandler> handlers = new HashMap<>();

    public VimShortcutManager(VimTextArea editor) {
        this.editor = editor;
        // Register handlers for each mode.
        handlers.put(VimModes.NORMAL, new NormalModeShortcutHandler(editor));
        handlers.put(VimModes.INSERT, new InsertModeShortcutHandler(editor));
        handlers.put(VimModes.VISUAL, new VisualModeShortcutHandler(editor));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Global key handling for mode switching.

        // Esc: return to READY mode from any mode.
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            editor.setMode(VimModes.NORMAL);
            e.consume();
            return;
        }

        // When in READY mode, allow switching to VISUAL or INSERT modes.
        if (editor.getCurrentMode() == VimModes.NORMAL) {
            char keyChar = e.getKeyChar();
            if (keyChar == 'v') {
                editor.setMode(VimModes.VISUAL);
                e.consume();
                return;
            } else if (keyChar == 'i') {
                editor.setMode(VimModes.INSERT);
                e.consume();
                return;
            }
        }

        // Delegate the key event to the handler for the current mode.
        VimModes mode = editor.getCurrentMode();
        VimModeShortcutHandler handler = handlers.get(mode);
        if (handler != null && handler.handleKeyPressed(e)) {
            e.consume();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // In READY mode, if the user typed a key that should only trigger a mode switch,
        // consume it so that it isn’t inserted into the text.
        if (editor.getCurrentMode() == VimModes.NORMAL) {
            char keyChar = e.getKeyChar();
            if (keyChar == 'v' || keyChar == 'i') {
                e.consume();
            }
        }
    }
}