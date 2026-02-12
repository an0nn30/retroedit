package com.github.an0nn30.jpad.lsp.features;

import com.github.an0nn30.jpad.logging.Logger;
import com.github.an0nn30.jpad.lsp.DocumentSyncManager;
import com.github.an0nn30.jpad.lsp.LspClient;
import com.github.an0nn30.jpad.ui.components.TextArea;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import javax.swing.Timer;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HoverProvider {

    private static final int HOVER_DEBOUNCE_MS = 500;

    private final LspClient lspClient;
    private final TextArea textArea;
    private final File file;
    private Timer hoverTimer;
    private MouseMotionListener motionListener;

    public HoverProvider(LspClient lspClient, TextArea textArea, File file) {
        this.lspClient = lspClient;
        this.textArea = textArea;
        this.file = file;
    }

    public void install() {
        motionListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (hoverTimer != null) {
                    hoverTimer.stop();
                }

                hoverTimer = new Timer(HOVER_DEBOUNCE_MS, evt -> {
                    int offset = textArea.viewToModel2D(e.getPoint());
                    if (offset >= 0) {
                        requestHover(offset);
                    }
                });
                hoverTimer.setRepeats(false);
                hoverTimer.start();
            }
        };
        textArea.addMouseMotionListener(motionListener);
    }

    public void uninstall() {
        if (motionListener != null) {
            textArea.removeMouseMotionListener(motionListener);
            motionListener = null;
        }
        if (hoverTimer != null) {
            hoverTimer.stop();
            hoverTimer = null;
        }
        textArea.setToolTipText(null);
    }

    private void requestHover(int offset) {
        if (lspClient.getStatus() != LspClient.Status.READY) return;

        Position position = DocumentSyncManager.offsetToPosition(textArea, offset);
        TextDocumentIdentifier docId = new TextDocumentIdentifier(file.toURI().toString());
        HoverParams params = new HoverParams(docId, position);

        lspClient.getServer().getTextDocumentService().hover(params)
                .thenAccept(hover -> {
                    if (hover != null && hover.getContents() != null) {
                        String text = extractHoverText(hover);
                        if (text != null && !text.isEmpty()) {
                            javax.swing.SwingUtilities.invokeLater(() ->
                                    textArea.setToolTipText(formatAsHtml(text)));
                        } else {
                            javax.swing.SwingUtilities.invokeLater(() ->
                                    textArea.setToolTipText(null));
                        }
                    } else {
                        javax.swing.SwingUtilities.invokeLater(() ->
                                textArea.setToolTipText(null));
                    }
                })
                .exceptionally(ex -> {
                    Logger.getInstance().debug(HoverProvider.class,
                            "Hover request failed: " + ex.getMessage());
                    return null;
                });
    }

    private String extractHoverText(Hover hover) {
        Either<List<Either<String, MarkedString>>, MarkupContent> contents = hover.getContents();
        if (contents.isRight()) {
            return contents.getRight().getValue();
        } else {
            StringBuilder sb = new StringBuilder();
            for (Either<String, MarkedString> item : contents.getLeft()) {
                if (item.isLeft()) {
                    sb.append(item.getLeft()).append("\n");
                } else {
                    sb.append(item.getRight().getValue()).append("\n");
                }
            }
            return sb.toString().trim();
        }
    }

    private String formatAsHtml(String markdown) {
        // Basic markdown to HTML conversion for tooltips
        String html = markdown
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");

        // Bold
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        // Code blocks
        html = html.replaceAll("`([^`]+)`", "<code>$1</code>");

        return "<html><body style='width: 400px; font-size: 11px;'>" + html + "</body></html>";
    }
}
