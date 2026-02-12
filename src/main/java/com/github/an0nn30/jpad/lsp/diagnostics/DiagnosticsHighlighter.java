package com.github.an0nn30.jpad.lsp.diagnostics;

import com.github.an0nn30.jpad.lsp.DocumentSyncManager;
import com.github.an0nn30.jpad.lsp.LspClient;
import com.github.an0nn30.jpad.ui.components.TextArea;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

import java.awt.*;
import java.util.List;

public class DiagnosticsHighlighter extends AbstractParser {

    private final LspClient lspClient;
    private final String uri;
    private final TextArea textArea;

    public DiagnosticsHighlighter(LspClient lspClient, String uri, TextArea textArea) {
        this.lspClient = lspClient;
        this.uri = uri;
        this.textArea = textArea;
    }

    @Override
    public ParseResult parse(RSyntaxDocument doc, String style) {
        DefaultParseResult result = new DefaultParseResult(this);

        List<Diagnostic> diags = lspClient.getDiagnostics(uri);
        if (diags == null || diags.isEmpty()) {
            return result;
        }

        for (Diagnostic diag : diags) {
            try {
                int startOffset = DocumentSyncManager.positionToOffset(textArea, diag.getRange().getStart());
                int endOffset = DocumentSyncManager.positionToOffset(textArea, diag.getRange().getEnd());
                int length = Math.max(1, endOffset - startOffset);
                int line = diag.getRange().getStart().getLine();

                DefaultParserNotice notice = new DefaultParserNotice(
                        this,
                        diag.getMessage(),
                        line,
                        startOffset,
                        length
                );
                notice.setLevel(mapSeverity(diag.getSeverity()));
                notice.setColor(getColorForSeverity(diag.getSeverity()));
                result.addNotice(notice);
            } catch (Exception e) {
                // Skip malformed diagnostics
            }
        }

        return result;
    }

    private ParserNotice.Level mapSeverity(DiagnosticSeverity severity) {
        if (severity == null) return ParserNotice.Level.INFO;
        return switch (severity) {
            case Error -> ParserNotice.Level.ERROR;
            case Warning -> ParserNotice.Level.WARNING;
            default -> ParserNotice.Level.INFO;
        };
    }

    private Color getColorForSeverity(DiagnosticSeverity severity) {
        if (severity == null) return Color.BLUE;
        return switch (severity) {
            case Error -> Color.RED;
            case Warning -> Color.YELLOW;
            default -> Color.BLUE;
        };
    }
}
