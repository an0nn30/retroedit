package com.github.an0nn30.jpad.lsp.completion;

import com.github.an0nn30.jpad.logging.Logger;
import com.github.an0nn30.jpad.lsp.DocumentSyncManager;
import com.github.an0nn30.jpad.lsp.LspClient;
import com.github.an0nn30.jpad.ui.components.TextArea;
import org.eclipse.lsp4j.*;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LspCompletionProvider extends DefaultCompletionProvider {

    private final LspClient lspClient;
    private final File file;
    private final DocumentSyncManager documentSyncManager;

    public LspCompletionProvider(LspClient lspClient, File file, DocumentSyncManager documentSyncManager) {
        this.lspClient = lspClient;
        this.file = file;
        this.documentSyncManager = documentSyncManager;
    }

    @Override
    public boolean isAutoActivateOkay(JTextComponent tc) {
        int pos = tc.getCaretPosition();
        if (pos > 0) {
            try {
                char ch = tc.getDocument().getText(pos - 1, 1).charAt(0);
                return Character.isLetterOrDigit(ch) || ch == '.' || ch == '_';
            } catch (BadLocationException e) {
                // fall through
            }
        }
        return false;
    }

    @Override
    protected List<Completion> getCompletionsImpl(JTextComponent comp) {
        if (lspClient.getStatus() != LspClient.Status.READY || !(comp instanceof TextArea textArea)) {
            Logger.getInstance().debug(LspCompletionProvider.class, "getCompletionsImpl: LSP not ready or not TextArea");
            return new ArrayList<>();
        }

        try {
            // Flush any pending document changes so JDT LS has the latest content
            String uri = file.toURI().toString();
            documentSyncManager.flushPendingChanges(uri, textArea);

            int caretOffset = textArea.getCaretPosition();
            Position position = DocumentSyncManager.offsetToPosition(textArea, caretOffset);

            Logger.getInstance().debug(LspCompletionProvider.class,
                    "Requesting completions at line " + position.getLine() + " char " + position.getCharacter());

            TextDocumentIdentifier docId = new TextDocumentIdentifier(file.toURI().toString());
            CompletionParams params = new CompletionParams(docId, position);
            params.setContext(new CompletionContext(CompletionTriggerKind.Invoked));

            Logger.getInstance().debug(LspCompletionProvider.class,
                    "Sending completion for URI: " + file.toURI().toString());

            var result = lspClient.getServer().getTextDocumentService()
                    .completion(params)
                    .get(5, TimeUnit.SECONDS);

            List<CompletionItem> items;
            if (result == null) {
                Logger.getInstance().debug(LspCompletionProvider.class, "Completion result is null");
                return new ArrayList<>();
            } else if (result.isLeft()) {
                items = result.getLeft();
                Logger.getInstance().debug(LspCompletionProvider.class,
                        "Completion result is Left (List), size: " + (items != null ? items.size() : "null"));
            } else {
                var completionList = result.getRight();
                items = completionList.getItems();
                Logger.getInstance().debug(LspCompletionProvider.class,
                        "Completion result is Right (CompletionList), isIncomplete: " +
                        completionList.isIncomplete() + ", size: " + (items != null ? items.size() : "null"));
            }

            Logger.getInstance().debug(LspCompletionProvider.class,
                    "Got " + items.size() + " completion items from LSP");

            String alreadyEntered = getAlreadyEnteredText(comp);
            List<Completion> completions = new ArrayList<>();

            for (CompletionItem item : items) {
                String insertText = getInsertText(item);
                String label = item.getLabel();

                // Use label as inputText (for matching/display), insertText as replacement
                BasicCompletion bc = new BasicCompletion(this, insertText, label, item.getDetail());
                bc.setRelevance(getRelevance(item.getKind()));

                // Only include if it matches what the user has typed so far
                if (alreadyEntered == null || alreadyEntered.isEmpty()
                        || startsWithIgnoreCase(label, alreadyEntered)) {
                    completions.add(bc);
                }
            }

            return completions;

        } catch (Exception e) {
            Logger.getInstance().debug(LspCompletionProvider.class,
                    "Completion request failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static boolean startsWithIgnoreCase(String s, String prefix) {
        return s.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private String getInsertText(CompletionItem item) {
        if (item.getTextEdit() != null) {
            if (item.getTextEdit().isLeft()) {
                return stripSnippetPlaceholders(item.getTextEdit().getLeft().getNewText());
            }
        }
        if (item.getInsertText() != null) {
            return stripSnippetPlaceholders(item.getInsertText());
        }
        return item.getLabel();
    }

    private String stripSnippetPlaceholders(String text) {
        return text.replaceAll("\\$\\{\\d+:([^}]*)}", "$1")
                   .replaceAll("\\$\\d+", "");
    }

    private int getRelevance(CompletionItemKind kind) {
        if (kind == null) return 0;
        return switch (kind) {
            case Variable -> 10;
            case Field -> 9;
            case Method -> 8;
            case Property -> 7;
            case Class -> 6;
            case Interface -> 5;
            case Keyword -> 4;
            case Snippet -> 3;
            case Module -> 2;
            default -> 1;
        };
    }
}
