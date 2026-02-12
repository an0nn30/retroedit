package com.github.an0nn30.jpad.lsp.diagnostics;

import com.github.an0nn30.jpad.event.EventBus;
import com.github.an0nn30.jpad.event.EventType;
import com.github.an0nn30.jpad.lsp.LspClient;
import com.github.an0nn30.jpad.ui.components.TextArea;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiagnosticsManager {

    private final LspClient lspClient;
    private final Map<String, DiagnosticsHighlighter> highlighters = new ConcurrentHashMap<>();
    private volatile TextArea activeTextArea;
    private volatile String activeUri;

    public DiagnosticsManager(LspClient lspClient) {
        this.lspClient = lspClient;

        EventBus.subscribe(EventType.LSP_DIAGNOSTICS.name(), event -> {
            String uri = (String) event.data();
            if (uri != null && uri.equals(activeUri) && activeTextArea != null) {
                SwingUtilities.invokeLater(() -> {
                    DiagnosticsHighlighter highlighter = highlighters.get(uri);
                    if (highlighter != null) {
                        activeTextArea.forceReparsing(highlighter);
                    }
                });
            }
        });
    }

    public void installHighlighter(TextArea textArea, String uri) {
        DiagnosticsHighlighter highlighter = new DiagnosticsHighlighter(lspClient, uri, textArea);
        textArea.addParser(highlighter);
        highlighters.put(uri, highlighter);
        this.activeTextArea = textArea;
        this.activeUri = uri;
    }

    public void setActiveDocument(TextArea textArea, String uri) {
        this.activeTextArea = textArea;
        this.activeUri = uri;
    }

    public void removeHighlighter(String uri) {
        highlighters.remove(uri);
        if (uri.equals(activeUri)) {
            activeUri = null;
            activeTextArea = null;
        }
    }

    public void shutdown() {
        highlighters.clear();
        activeTextArea = null;
        activeUri = null;
    }
}
