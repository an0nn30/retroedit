package com.github.an0nn30.jpad.lsp.completion;

import com.github.an0nn30.jpad.lsp.DocumentSyncManager;
import com.github.an0nn30.jpad.lsp.LspClient;
import com.github.an0nn30.jpad.ui.components.TextArea;
import org.fife.ui.autocomplete.AutoCompletion;

import java.io.File;

public class LspAutoCompletionInstaller {

    public static AutoCompletion install(TextArea textArea, LspClient lspClient, File file, DocumentSyncManager documentSyncManager) {
        LspCompletionProvider provider = new LspCompletionProvider(lspClient, file, documentSyncManager);
        // Enable auto-activation after letters and dot character
        provider.setAutoActivationRules(true, ".");
        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationEnabled(true);
        ac.setAutoActivationDelay(300);
        ac.setShowDescWindow(true);
        ac.install(textArea);
        return ac;
    }
}
