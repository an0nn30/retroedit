package com.github.an0nn30.retroedit.ui.actions;

import com.github.an0nn30.retroedit.ui.Editor;
import org.fife.rsta.ui.GoToDialog;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;

public class GoToLineAction extends AbstractAction {

    private final Editor editor;

    public GoToLineAction(Editor editor) {
        super("Go To Line...");
        this.editor = editor;
        int shortcut = editor.getToolkit().getMenuShortcutKeyMaskEx();
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Ensure search dialogs are hidden
        if (editor.getSearchController().isFindDialogVisible()) {
            editor.getSearchController().hideFindDialog();
        }
        if (editor.getSearchController().isReplaceDialogVisible()) {
            editor.getSearchController().hideReplaceDialog();
        }
        GoToDialog dialog = new GoToDialog(editor);
        int maxLine = editor.getTabManager().getActiveTextArea().getLineCount();
        dialog.setMaxLineNumberAllowed(maxLine);
        dialog.setVisible(true);
        int line = dialog.getLineNumber();
        if (line > 0) {
            try {
                editor.getTabManager().getActiveTextArea().setCaretPosition(
                        editor.getTabManager().getActiveTextArea().getLineStartOffset(line - 1)
                );
            } catch (BadLocationException ble) {
                UIManager.getLookAndFeel().provideErrorFeedback(editor.getTabManager().getActiveTextArea());
                ble.printStackTrace();
            }
        }
    }
}
