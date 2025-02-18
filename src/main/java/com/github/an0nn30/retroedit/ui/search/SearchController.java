package com.github.an0nn30.retroedit.ui.search;


import com.github.an0nn30.retroedit.ui.Editor;
import com.github.an0nn30.retroedit.ui.components.TextArea;
import com.github.an0nn30.retroedit.ui.theme.ThemeManager;
import org.fife.rsta.ui.search.*;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;

public class SearchController implements SearchListener {

    private final Editor editor;
    private final FindDialog findDialog;
    private final ReplaceDialog replaceDialog;
    private final FindToolBar findToolBar;
    private final ReplaceToolBar replaceToolBar;

    public SearchController(Editor editor) {
        this.editor = editor;
        // Initialize dialogs; pass `this` as the listener if you wish for SearchController to handle events.
        findDialog = new FindDialog(editor, this);
        replaceDialog = new ReplaceDialog(editor, this);

        // Tie the dialogs together.
        SearchContext context = findDialog.getSearchContext();
        replaceDialog.setSearchContext(context);

        // Optionally, create search toolbars.
        findToolBar = new FindToolBar(this);
        findToolBar.setSearchContext(context);
        replaceToolBar = new ReplaceToolBar(this);
        replaceToolBar.setSearchContext(context);
    }

    public void showFindDialog() {
        findDialog.setVisible(true);
    }

    public void hideFindDialog() {
        findDialog.setVisible(false);
    }

    public void showReplaceDialog() {
        replaceDialog.setVisible(true);
    }

    public void hideReplaceDialog() {
        replaceDialog.setVisible(false);
    }

    public boolean isFindDialogVisible() {
        return findDialog.isVisible();
    }

    public boolean isReplaceDialogVisible() {
        return replaceDialog.isVisible();
    }

    // Handle search events coming from the dialogs/toolbars.
    @Override
    public void searchEvent(SearchEvent e) {
        SearchContext context = e.getSearchContext();
        TextArea activeTextArea = editor.getTabManager().getActiveTextArea();
        SearchResult result = null;
        switch (e.getType()) {
            case MARK_ALL:
                result = SearchEngine.markAll(activeTextArea, context);
                break;
            case FIND:
                result = SearchEngine.find(activeTextArea, context);
                if (!result.wasFound() || result.isWrapped()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(activeTextArea);
                }
                break;
            case REPLACE:
                result = SearchEngine.replace(activeTextArea, context);
                if (!result.wasFound() || result.isWrapped()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(activeTextArea);
                }
                break;
            case REPLACE_ALL:
                result = SearchEngine.replaceAll(activeTextArea, context);
                JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
                break;
            default:
                break;
        }
        // Optionally, update a status bar or other UI elements based on `result`
    }

    @Override
    public String getSelectedText() {
        return this.editor.getTabManager().getActiveTextArea().getSelectedText();
    }
}

