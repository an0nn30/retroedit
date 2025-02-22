package com.github.an0nn30.retroedit.ui.actions;

import com.github.an0nn30.retroedit.ui.EditorFrame;
import com.github.an0nn30.retroedit.ui.search.SearchController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * An action that opens the replace dialog in the editor.
 * If the find dialog is visible, it is hidden before showing the replace dialog.
 */
public class ReplaceDialogAction extends AbstractAction {

    private final EditorFrame editorFrame;

    /**
     * Constructs a ReplaceDialogAction with the specified EditorFrame.
     * The action is labeled "Replace..." and is bound to the platform-specific shortcut (typically Ctrl+H or Command+H).
     *
     * @param editorFrame the parent EditorFrame.
     */
    public ReplaceDialogAction(EditorFrame editorFrame) {
        super("Replace...");
        this.editorFrame = editorFrame;
        int shortcut = editorFrame.getToolkit().getMenuShortcutKeyMaskEx();
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, shortcut));
    }

    /**
     * Invoked when an action occurs. This method hides the find dialog (if visible)
     * and then shows the replace dialog.
     *
     * @param e the action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        performReplaceAction();
    }

    /**
     * Performs the replace action by hiding the find dialog (if it is visible)
     * and then displaying the replace dialog.
     */
    private void performReplaceAction() {
        SearchController searchController = editorFrame.getSearchController();
        if (searchController.isFindDialogVisible()) {
            searchController.hideFindDialog();
        }
        searchController.showReplaceDialog();
    }
}