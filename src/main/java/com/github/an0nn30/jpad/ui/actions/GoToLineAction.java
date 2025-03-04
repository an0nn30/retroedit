package com.github.an0nn30.jpad.ui.actions;

import com.github.an0nn30.jpad.ui.EditorFrame;
import org.fife.rsta.ui.GoToDialog;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * An action that displays a "Go To Line" dialog for navigating within the active text area.
 * If any find or replace dialogs are visible, they are hidden before showing the "Go To" dialog.
 */
public class GoToLineAction extends AbstractAction {

    private final EditorFrame editorFrame;

    /**
     * Constructs a new GoToLineAction with the specified EditorFrame.
     * The action is labeled "Go To Line..." and is bound to the platform-specific shortcut (typically Ctrl+L or Command+L).
     *
     * @param editorFrame the parent EditorFrame.
     */
    public GoToLineAction(EditorFrame editorFrame) {
        super("Go To Line...");
        this.editorFrame = editorFrame;
        int shortcut = editorFrame.getToolkit().getMenuShortcutKeyMaskEx();
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));
    }

    /**
     * Invoked when the action occurs. It hides any open find or replace dialogs, creates and shows the "Go To" dialog,
     * and moves the caret in the active text area to the specified line number.
     *
     * @param e the action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        hideSearchDialogs();
        GoToDialog dialog = createGoToDialog();
        dialog.setVisible(true);
        int line = dialog.getLineNumber();
        if (line > 0) {
            goToLine(line);
        }
    }

    /**
     * Hides any visible find or replace dialogs.
     */
    private void hideSearchDialogs() {
        if (editorFrame.getSearchController().isFindDialogVisible()) {
            editorFrame.getSearchController().hideFindDialog();
        }
        if (editorFrame.getSearchController().isReplaceDialogVisible()) {
            editorFrame.getSearchController().hideReplaceDialog();
        }
    }

    /**
     * Creates and configures a GoToDialog for the active text area.
     *
     * @return the configured GoToDialog.
     */
    private GoToDialog createGoToDialog() {
        GoToDialog dialog = new GoToDialog(editorFrame);
        var activeTextArea = editorFrame.getTabManager().getActiveTextArea();
        if (activeTextArea != null) {
            int maxLine = activeTextArea.getLineCount();
            dialog.setMaxLineNumberAllowed(maxLine);
        }
        return dialog;
    }

    /**
     * Moves the caret of the active text area to the start of the specified line.
     *
     * @param line the 1-indexed line number to navigate to.
     */
    private void goToLine(int line) {
        var activeTextArea = editorFrame.getTabManager().getActiveTextArea();
        if (activeTextArea == null) {
            return;
        }
        try {
            int caretPos = activeTextArea.getLineStartOffset(line - 1);
            activeTextArea.setCaretPosition(caretPos);
        } catch (BadLocationException ble) {
            UIManager.getLookAndFeel().provideErrorFeedback(activeTextArea);
            ble.printStackTrace();
        }
    }
}