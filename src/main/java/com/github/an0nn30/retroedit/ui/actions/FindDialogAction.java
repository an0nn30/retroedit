package com.github.an0nn30.retroedit.ui.actions;

import com.github.an0nn30.retroedit.ui.EditorFrame;
import com.github.an0nn30.retroedit.ui.search.SearchController;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class FindDialogAction extends AbstractAction {

    private final EditorFrame editorFrame;

    public FindDialogAction(EditorFrame editorFrame) {
        super("Find...");
        this.editorFrame = editorFrame;
        int shortcut = editorFrame.getToolkit().getMenuShortcutKeyMaskEx();
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcut));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SearchController searchController = editorFrame.getSearchController();
        if (searchController.isReplaceDialogVisible()) {
            searchController.hideReplaceDialog();
        }
        searchController.showFindDialog();
    }
}

