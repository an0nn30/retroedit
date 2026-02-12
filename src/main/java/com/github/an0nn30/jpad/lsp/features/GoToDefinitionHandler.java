package com.github.an0nn30.jpad.lsp.features;

import com.github.an0nn30.jpad.logging.Logger;
import com.github.an0nn30.jpad.lsp.DocumentSyncManager;
import com.github.an0nn30.jpad.lsp.LspClient;
import com.github.an0nn30.jpad.ui.TextAreaTabManager;
import com.github.an0nn30.jpad.ui.components.TextArea;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoToDefinitionHandler {

    private final LspClient lspClient;
    private final TextArea textArea;
    private final File file;
    private final TextAreaTabManager tabManager;
    private MouseListener mouseListener;

    public GoToDefinitionHandler(LspClient lspClient, TextArea textArea, File file,
                                  TextAreaTabManager tabManager) {
        this.lspClient = lspClient;
        this.textArea = textArea;
        this.file = file;
        this.tabManager = tabManager;
    }

    public void install() {
        mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int modifierMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
                if ((e.getModifiersEx() & modifierMask) != 0 && e.getClickCount() == 1) {
                    int offset = textArea.viewToModel2D(e.getPoint());
                    if (offset >= 0) {
                        goToDefinition(offset);
                    }
                }
            }
        };
        textArea.addMouseListener(mouseListener);
    }

    public void uninstall() {
        if (mouseListener != null) {
            textArea.removeMouseListener(mouseListener);
            mouseListener = null;
        }
    }

    private void goToDefinition(int offset) {
        if (lspClient.getStatus() != LspClient.Status.READY) return;

        Position position = DocumentSyncManager.offsetToPosition(textArea, offset);
        TextDocumentIdentifier docId = new TextDocumentIdentifier(file.toURI().toString());
        DefinitionParams params = new DefinitionParams(docId, position);

        lspClient.getServer().getTextDocumentService().definition(params)
                .thenAccept(result -> {
                    if (result == null) return;

                    Location target = null;
                    if (result.isLeft()) {
                        List<? extends Location> locations = result.getLeft();
                        if (!locations.isEmpty()) {
                            target = locations.get(0);
                        }
                    } else {
                        List<? extends LocationLink> links = result.getRight();
                        if (!links.isEmpty()) {
                            LocationLink link = links.get(0);
                            target = new Location(link.getTargetUri(), link.getTargetRange());
                        }
                    }

                    if (target != null) {
                        navigateToLocation(target);
                    }
                })
                .exceptionally(ex -> {
                    Logger.getInstance().debug(GoToDefinitionHandler.class,
                            "Go-to-definition failed: " + ex.getMessage());
                    return null;
                });
    }

    private void navigateToLocation(Location location) {
        try {
            URI uri = new URI(location.getUri());
            File targetFile = new File(uri);
            int targetLine = location.getRange().getStart().getLine();
            int targetChar = location.getRange().getStart().getCharacter();

            javax.swing.SwingUtilities.invokeLater(() -> {
                tabManager.openFile(targetFile);
                TextArea activeTextArea = tabManager.getActiveTextArea();
                if (activeTextArea != null) {
                    try {
                        int lineStart = activeTextArea.getLineStartOffset(targetLine);
                        activeTextArea.setCaretPosition(lineStart + targetChar);
                        activeTextArea.requestFocus();
                    } catch (Exception e) {
                        Logger.getInstance().warn(GoToDefinitionHandler.class,
                                "Failed to set caret: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Logger.getInstance().warn(GoToDefinitionHandler.class,
                    "Failed to navigate: " + e.getMessage());
        }
    }
}
