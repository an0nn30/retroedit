package com.github.an0nn30.retroedit.ui.actions;

import com.github.an0nn30.retroedit.ui.EditorFrame;
import org.fife.rsta.ui.GoToDialog;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;

public class GoToLineAction extends AbstractAction {

    private final EditorFrame editorFrame;

    public GoToLineAction(EditorFrame editorFrame) {
        super("Go To Line...");
        this.editorFrame = editorFrame;
        int shortcut = editorFrame.getToolkit().getMenuShortcutKeyMaskEx();
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Ensure search dialogs are hidden
        if (editorFrame.getSearchController().isFindDialogVisible()) {
            editorFrame.getSearchController().hideFindDialog();
        }
        if (editorFrame.getSearchController().isReplaceDialogVisible()) {
            editorFrame.getSearchController().hideReplaceDialog();
        }
        GoToDialog dialog = new GoToDialog(editorFrame);
        int maxLine = editorFrame.getTabManager().getActiveTextArea().getLineCount();
        dialog.setMaxLineNumberAllowed(maxLine);
        dialog.setVisible(true);
        int line = dialog.getLineNumber();
        if (line > 0) {
            try {
                editorFrame.getTabManager().getActiveTextArea().setCaretPosition(
                        editorFrame.getTabManager().getActiveTextArea().getLineStartOffset(line - 1)
                );
            } catch (BadLocationException ble) {
                UIManager.getLookAndFeel().provideErrorFeedback(editorFrame.getTabManager().getActiveTextArea());
                ble.printStackTrace();
            }
        }
    }
}
