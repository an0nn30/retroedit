package com.github.an0nn30.jpad.ui.search;

import com.github.an0nn30.jpad.ui.EditorFrame;
import com.github.an0nn30.jpad.ui.components.TextArea;
import org.fife.rsta.ui.search.*;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;

/**
 * The SearchController class handles find/replace functionality for the editor.
 * It initializes find and replace dialogs, as well as their toolbars,
 * and delegates search operations to the underlying SearchEngine.
 */
public class SearchController implements SearchListener {

    private final EditorFrame editorFrame;
    private final FindDialog findDialog;
    private final ReplaceDialog replaceDialog;
    private final FindToolBar findToolBar;
    private final ReplaceToolBar replaceToolBar;

    /**
     * Constructs a SearchController with the given EditorFrame.
     * It initializes the find/replace dialogs and their shared search context.
     *
     * @param editorFrame the parent editor frame.
     */
    public SearchController(EditorFrame editorFrame) {
        this.editorFrame = editorFrame;
        // Initialize dialogs with this controller as the listener.
        findDialog = new FindDialog(editorFrame, this);
        replaceDialog = new ReplaceDialog(editorFrame, this);

        // Share the search context between dialogs.
        SearchContext context = findDialog.getSearchContext();
        replaceDialog.setSearchContext(context);

        // Optionally, initialize the search toolbars.
        findToolBar = new FindToolBar(this);
        findToolBar.setSearchContext(context);
        replaceToolBar = new ReplaceToolBar(this);
        replaceToolBar.setSearchContext(context);
    }

    /**
     * Displays the find dialog.
     */
    public void showFindDialog() {
        findDialog.setVisible(true);
    }

    /**
     * Hides the find dialog.
     */
    public void hideFindDialog() {
        findDialog.setVisible(false);
    }

    /**
     * Displays the replace dialog.
     */
    public void showReplaceDialog() {
        replaceDialog.setVisible(true);
    }

    /**
     * Hides the replace dialog.
     */
    public void hideReplaceDialog() {
        replaceDialog.setVisible(false);
    }

    /**
     * Returns whether the find dialog is currently visible.
     *
     * @return true if the find dialog is visible; false otherwise.
     */
    public boolean isFindDialogVisible() {
        return findDialog.isVisible();
    }

    /**
     * Returns whether the replace dialog is currently visible.
     *
     * @return true if the replace dialog is visible; false otherwise.
     */
    public boolean isReplaceDialogVisible() {
        return replaceDialog.isVisible();
    }

    /**
     * Handles search events generated from the dialogs or toolbars.
     * Depending on the event type, delegates the appropriate search operation to the SearchEngine.
     *
     * @param e the SearchEvent containing the search context and type.
     */
    @Override
    public void searchEvent(SearchEvent e) {
        SearchContext context = e.getSearchContext();
        TextArea activeTextArea = editorFrame.getTabManager().getActiveTextArea();
        if (activeTextArea == null) {
            return; // No active text area to search.
        }
        SearchResult result = performSearchOperation(e.getType(), activeTextArea, context);
        // Provide error feedback for FIND and REPLACE operations if not found or wrapped.
        if ((e.getType() == SearchEvent.Type.FIND || e.getType() == SearchEvent.Type.REPLACE)
                && (result == null || !result.wasFound() || result.isWrapped())) {
            UIManager.getLookAndFeel().provideErrorFeedback(activeTextArea);
        }
        // Notify the user about the number of occurrences replaced in a REPLACE_ALL operation.
        if (e.getType() == SearchEvent.Type.REPLACE_ALL && result != null) {
            JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
        }
        // Additional status updates (e.g., status bar messages) can be added here.
    }

    /**
     * Performs the search operation corresponding to the specified type.
     *
     * @param type           the type of search event.
     * @param activeTextArea the active text area to search within.
     * @param context        the search context.
     * @return the SearchResult of the operation, or null if not applicable.
     */
    private SearchResult performSearchOperation(SearchEvent.Type type, TextArea activeTextArea, SearchContext context) {
        SearchResult result = null;
        switch (type) {
            case MARK_ALL:
                result = SearchEngine.markAll(activeTextArea, context);
                break;
            case FIND:
                result = SearchEngine.find(activeTextArea, context);
                break;
            case REPLACE:
                result = SearchEngine.replace(activeTextArea, context);
                break;
            case REPLACE_ALL:
                result = SearchEngine.replaceAll(activeTextArea, context);
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Returns the currently selected text from the active text area.
     *
     * @return the selected text, or null if there is no active text area or no selection.
     */
    @Override
    public String getSelectedText() {
        TextArea activeTextArea = editorFrame.getTabManager().getActiveTextArea();
        return activeTextArea != null ? activeTextArea.getSelectedText() : null;
    }
}