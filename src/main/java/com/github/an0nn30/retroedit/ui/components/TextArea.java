package com.github.an0nn30.retroedit.ui.components;

import com.github.an0nn30.retroedit.event.EventRecord;
import com.github.an0nn30.retroedit.event.EventBus;
import com.github.an0nn30.retroedit.event.EventType;
import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import com.github.an0nn30.retroedit.ui.Editor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextArea extends RSyntaxTextArea {

    private Map<String, String> syntaxMap;
    private File activeFile;
    private final Editor editor;

    public TextArea(Editor editor) {
        super();
        this.editor = editor;
        initSyntaxMap();
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
            theme.apply(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initFontSizeAndFamily();
        setTabSize(4);
        // Subscribe only to syntax highlighting updates.
        EventBus.subscribe(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(), (EventRecord<Object> eventRecord) -> {
            String fileType = (String) eventRecord.data();
            setSyntaxEditingStyle(fileType);
        });
        EventBus.subscribe(EventType.FONT_FAMILY_CHANGED.name(), (EventRecord<Object> eventRecord) -> {
            String fontFamily = (String) eventRecord.data();
            Logger.getInstance().info(TextArea.class, "Font family changed to: " + fontFamily);
            setFont(new java.awt.Font(fontFamily, java.awt.Font.PLAIN, getFont().getSize()));
        });
        EventBus.subscribe(EventType.FONT_SIZE_CHANGED.name(), (EventRecord<Object> eventRecord) -> {
            int fontSize = (int) eventRecord.data();
            Logger.getInstance().info(TextArea.class, "Font size changed to: " + fontSize);
            setFont(new java.awt.Font(getFont().getFamily(), java.awt.Font.PLAIN, fontSize));
        });

        CompletionProvider provider = createCompletionProvider();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(this);
        ac.setAutoActivationEnabled(true);
        ac.setAutoActivationDelay(0);
    }

    private void initSyntaxMap() {
        syntaxMap = new HashMap<>();
        syntaxMap.put("java", SyntaxConstants.SYNTAX_STYLE_JAVA);
        syntaxMap.put("py", SyntaxConstants.SYNTAX_STYLE_PYTHON);
        syntaxMap.put("c", SyntaxConstants.SYNTAX_STYLE_C);
        syntaxMap.put("cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
        syntaxMap.put("json", SyntaxConstants.SYNTAX_STYLE_JSON);
        syntaxMap.put("go", SyntaxConstants.SYNTAX_STYLE_GO);
    }

    public void initFontSizeAndFamily() {
        Logger.getInstance().info(TextArea.class, "Settings font size and family: " + Settings.getInstance().getEditorFontSize() + " " + Settings.getInstance().getEditorFontFamily());
        setFont(new java.awt.Font(Settings.getInstance().getEditorFontFamily(), java.awt.Font.PLAIN, Settings.getInstance().getEditorFontSize()));
    }

    public Map<String, String> getSyntaxMap() {
        return syntaxMap;
    }

    public File getActiveFile() {
        return activeFile;
    }

    public void setActiveFile(File activeFile) {
        this.activeFile = activeFile;
    }

    /**
     * Create a simple provider that adds some Java-related completions.
     */
    private CompletionProvider createCompletionProvider() {

        // A DefaultCompletionProvider is the simplest concrete implementation
        // of CompletionProvider. This provider has no understanding of
        // language semantics. It simply checks the text entered up to the
        // caret position for a match against known completions. This is all
        // that is needed in the majority of cases.
        DefaultCompletionProvider provider = new DefaultCompletionProvider();

        // Add completions for all Java keywords. A BasicCompletion is just
        // a straightforward word completion.
        provider.addCompletion(new BasicCompletion(provider, "abstract"));
        provider.addCompletion(new BasicCompletion(provider, "assert"));
        provider.addCompletion(new BasicCompletion(provider, "break"));
        provider.addCompletion(new BasicCompletion(provider, "case"));
        // ... etc ...
        provider.addCompletion(new BasicCompletion(provider, "transient"));
        provider.addCompletion(new BasicCompletion(provider, "try"));
        provider.addCompletion(new BasicCompletion(provider, "void"));
        provider.addCompletion(new BasicCompletion(provider, "volatile"));
        provider.addCompletion(new BasicCompletion(provider, "while"));

        // Add a couple of "shorthand" completions. These completions don't
        // require the input text to be the same thing as the replacement text.
        provider.addCompletion(new ShorthandCompletion(provider, "sysout",
                "System.out.println(", "System.out.println("));
        provider.addCompletion(new ShorthandCompletion(provider, "syserr",
                "System.err.println(", "System.err.println("));

        return provider;

    }
}
