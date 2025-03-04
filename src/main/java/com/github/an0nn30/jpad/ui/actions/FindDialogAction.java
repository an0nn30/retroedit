package com.github.an0nn30.jpad.ui.actions;

import com.github.an0nn30.jpad.ui.EditorFrame;
import com.github.an0nn30.jpad.ui.search.SearchController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * An action that opens the find dialog in the editor.
 * If the replace dialog is visible, it hides it before showing the find dialog.
 */
public class FindDialogAction extends AbstractAction {

    private final EditorFrame editorFrame;

    /**
     * Constructs a FindDialogAction with the specified EditorFrame.
     * The action is labeled "Find..." and is bound to the platform-specific find shortcut.
     *
     * @param editorFrame the parent editor frame.
     */
    public FindDialogAction(EditorFrame editorFrame) {
        super("Find...");
        this.editorFrame = editorFrame;
        int shortcut = editorFrame.getToolkit().getMenuShortcutKeyMaskEx();
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcut));
    }

    /**
     * Invoked when an action occurs. This method toggles the find dialog in the editor.
     *
     * @param e the action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        performFindAction();
    }

    /**
     * Handles the logic to display the find dialog.
     * If the replace dialog is currently visible, it hides it before showing the find dialog.
     */
    private void performFindAction() {
        SearchController searchController = editorFrame.getSearchController();
        if (searchController.isReplaceDialogVisible()) {
            searchController.hideReplaceDialog();
        }
        searchController.showFindDialog();
    }
}