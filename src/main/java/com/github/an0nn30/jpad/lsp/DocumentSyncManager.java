package com.github.an0nn30.jpad.lsp;

import com.github.an0nn30.jpad.logging.Logger;
import com.github.an0nn30.jpad.ui.components.TextArea;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageServer;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentSyncManager {

    private final LspClient lspClient;
    private final Map<String, DocumentListener> listeners = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> versions = new ConcurrentHashMap<>();
    private final Map<String, Timer> debounceTimers = new ConcurrentHashMap<>();

    private static final int DEBOUNCE_MS = 300;

    public DocumentSyncManager(LspClient lspClient) {
        this.lspClient = lspClient;
    }

    public void documentOpened(TextArea textArea, File file) {
        if (lspClient.getStatus() != LspClient.Status.READY) {
            Logger.getInstance().debug(DocumentSyncManager.class, "documentOpened: LSP not ready, skipping");
            return;
        }

        String uri = file.toURI().toString();
        String text = textArea.getText();
        AtomicInteger version = new AtomicInteger(1);
        versions.put(uri, version);

        Logger.getInstance().info(DocumentSyncManager.class,
                "Sending didOpen for " + uri + " (content length: " + text.length() + ")");

        TextDocumentItem item = new TextDocumentItem();
        item.setUri(uri);
        item.setLanguageId("java");
        item.setVersion(version.get());
        item.setText(text);

        lspClient.getServer().getTextDocumentService()
                .didOpen(new DidOpenTextDocumentParams(item));

        // Attach change listener with debouncing
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { scheduleChange(textArea, uri); }
            @Override
            public void removeUpdate(DocumentEvent e) { scheduleChange(textArea, uri); }
            @Override
            public void changedUpdate(DocumentEvent e) { /* attribute changes, ignore */ }
        };
        textArea.getDocument().addDocumentListener(listener);
        listeners.put(uri, listener);
    }

    public void documentClosed(TextArea textArea, File file) {
        if (lspClient.getStatus() != LspClient.Status.READY) return;

        String uri = file.toURI().toString();

        // Remove listener
        DocumentListener listener = listeners.remove(uri);
        if (listener != null) {
            textArea.getDocument().removeDocumentListener(listener);
        }

        // Cancel any pending debounce
        Timer timer = debounceTimers.remove(uri);
        if (timer != null) {
            timer.stop();
        }

        versions.remove(uri);

        TextDocumentIdentifier id = new TextDocumentIdentifier(uri);
        lspClient.getServer().getTextDocumentService()
                .didClose(new DidCloseTextDocumentParams(id));
    }

    public void documentSaved(TextArea textArea, File file) {
        if (lspClient.getStatus() != LspClient.Status.READY) return;

        String uri = file.toURI().toString();
        TextDocumentIdentifier id = new TextDocumentIdentifier(uri);

        DidSaveTextDocumentParams params = new DidSaveTextDocumentParams(id);
        params.setText(textArea.getText());
        lspClient.getServer().getTextDocumentService().didSave(params);
    }

    private void scheduleChange(TextArea textArea, String uri) {
        Timer existing = debounceTimers.get(uri);
        if (existing != null) {
            existing.stop();
        }

        Timer timer = new Timer(DEBOUNCE_MS, e -> sendFullChange(textArea, uri));
        timer.setRepeats(false);
        debounceTimers.put(uri, timer);
        timer.start();
    }

    private void sendFullChange(TextArea textArea, String uri) {
        if (lspClient.getStatus() != LspClient.Status.READY) return;

        AtomicInteger version = versions.get(uri);
        if (version == null) return;

        int newVersion = version.incrementAndGet();
        VersionedTextDocumentIdentifier versionedId = new VersionedTextDocumentIdentifier();
        versionedId.setUri(uri);
        versionedId.setVersion(newVersion);

        TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent();
        change.setText(textArea.getText());

        DidChangeTextDocumentParams params = new DidChangeTextDocumentParams();
        params.setTextDocument(versionedId);
        params.setContentChanges(List.of(change));

        lspClient.getServer().getTextDocumentService().didChange(params);
    }

    /**
     * Immediately sends any pending document changes for the given URI,
     * cancelling the debounce timer. Call this before completion requests
     * to ensure the server has the latest content.
     */
    public void flushPendingChanges(String uri, TextArea textArea) {
        Timer timer = debounceTimers.remove(uri);
        if (timer != null) {
            timer.stop();
        }
        // Always send current content to ensure server is in sync
        if (versions.containsKey(uri)) {
            sendFullChange(textArea, uri);
        }
    }

    public void shutdown() {
        debounceTimers.values().forEach(Timer::stop);
        debounceTimers.clear();
        listeners.clear();
        versions.clear();
    }

    // Coordinate conversion utilities

    public static Position offsetToPosition(TextArea textArea, int offset) {
        try {
            int line = textArea.getLineOfOffset(offset);
            int lineStart = textArea.getLineStartOffset(line);
            int character = offset - lineStart;
            return new Position(line, character);
        } catch (BadLocationException e) {
            return new Position(0, 0);
        }
    }

    public static int positionToOffset(TextArea textArea, Position position) {
        try {
            int lineStart = textArea.getLineStartOffset(position.getLine());
            return lineStart + position.getCharacter();
        } catch (BadLocationException e) {
            return 0;
        }
    }
}
