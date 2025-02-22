package com.github.an0nn30.retroedit.ui.components;

import com.github.an0nn30.retroedit.event.EventRecord;
import com.github.an0nn30.retroedit.event.EventBus;
import com.github.an0nn30.retroedit.event.EventType;
import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import com.github.an0nn30.retroedit.ui.theme.ThemeManager;
import com.github.an0nn30.retroedit.ui.EditorFrame;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A custom text area component that extends RSyntaxTextArea.
 * <p>
 * This class supports syntax highlighting based on file types, auto-completion, and dynamic updates
 * from application settings via the EventBus.
 * </p>
 */
public class TextArea extends RSyntaxTextArea {

    /** Mapping of file extensions to syntax highlighting styles. */
    private Map<String, String> syntaxMap;
    /** The file currently loaded in this text area. */
    private File activeFile;
    /** The parent editor frame. */
    private final EditorFrame editorFrame;

    /**
     * Constructs a new TextArea associated with the specified EditorFrame.
     *
     * @param editorFrame the parent editor frame.
     */
    public TextArea(EditorFrame editorFrame) {
        super();
        this.editorFrame = editorFrame;
        initSyntaxMap();
        loadDefaultTheme();
        initFontSizeAndFamily();
        applyColorScheme(Settings.getInstance().getInterfaceTheme());
        setTabSize(4);
        setupEventSubscriptions();
        initializeAutoCompletion();
    }

    /**
     * Initializes the mapping between file extensions and RSyntaxTextArea syntax styles.
     */
    private void initSyntaxMap() {
        syntaxMap = new HashMap<>();
        syntaxMap.put("java", SyntaxConstants.SYNTAX_STYLE_JAVA);
        syntaxMap.put("py", SyntaxConstants.SYNTAX_STYLE_PYTHON);
        syntaxMap.put("c", SyntaxConstants.SYNTAX_STYLE_C);
        syntaxMap.put("cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
        syntaxMap.put("json", SyntaxConstants.SYNTAX_STYLE_JSON);
        syntaxMap.put("go", SyntaxConstants.SYNTAX_STYLE_GO);
    }

    /**
     * Loads and applies the default theme (idea.xml) from RSyntaxTextArea's theme resources.
     */
    private void loadDefaultTheme() {
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
            theme.apply(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the font size and family based on the application settings.
     */
    public void initFontSizeAndFamily() {
        Logger.getInstance().info(TextArea.class,
                "Settings font size and family: " + Settings.getInstance().getEditorFontSize() + " " +
                        Settings.getInstance().getEditorFontFamily());
        setFont(new java.awt.Font(Settings.getInstance().getEditorFontFamily(),
                java.awt.Font.PLAIN, Settings.getInstance().getEditorFontSize()));
    }

    /**
     * Returns the mapping of file extensions to syntax highlighting styles.
     *
     * @return the syntax map.
     */
    public Map<String, String> getSyntaxMap() {
        return syntaxMap;
    }

    /**
     * Returns the file currently loaded in this text area.
     *
     * @return the active file.
     */
    public File getActiveFile() {
        return activeFile;
    }

    /**
     * Sets the active file for this text area.
     *
     * @param activeFile the file to set as active.
     */
    public void setActiveFile(File activeFile) {
        this.activeFile = activeFile;
    }

    /**
     * Subscribes to various EventBus events to handle dynamic updates
     * (e.g., syntax highlighting, font family/size, theme changes).
     */
    private void setupEventSubscriptions() {
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
        EventBus.subscribe(EventType.THEME_CHANGED.name(), (EventRecord<Object> eventRecord) ->
                applyColorScheme((String) eventRecord.data()));
    }

    /**
     * Initializes the auto-completion feature using a custom CompletionProvider.
     */
    private void initializeAutoCompletion() {
        CompletionProvider provider = createCompletionProvider();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(this);
        ac.setAutoActivationEnabled(true);
        ac.setAutoActivationDelay(0);
    }

    /**
     * Creates a CompletionProvider for auto-completion, including Java keyword completions
     * and shorthand completions for System.out.println and System.err.println.
     *
     * @return a CompletionProvider instance.
     */
    private CompletionProvider createCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();

        // Add Java keyword completions.
        provider.addCompletion(new BasicCompletion(provider, "abstract"));
        provider.addCompletion(new BasicCompletion(provider, "assert"));
        provider.addCompletion(new BasicCompletion(provider, "break"));
        provider.addCompletion(new BasicCompletion(provider, "case"));
        // ... (Additional keywords can be added here) ...
        provider.addCompletion(new BasicCompletion(provider, "transient"));
        provider.addCompletion(new BasicCompletion(provider, "try"));
        provider.addCompletion(new BasicCompletion(provider, "void"));
        provider.addCompletion(new BasicCompletion(provider, "volatile"));
        provider.addCompletion(new BasicCompletion(provider, "while"));

        // Add shorthand completions.
        provider.addCompletion(new ShorthandCompletion(provider, "sysout",
                "System.out.println(", "System.out.println("));
        provider.addCompletion(new ShorthandCompletion(provider, "syserr",
                "System.err.println(", "System.err.println("));

        return provider;
    }

    /**
     * Applies a color scheme to this text area based on the provided scheme.
     * Supports "retro" (or "light") and "dark" themes.
     *
     * @param scheme the desired color scheme.
     */
    private void applyColorScheme(String scheme) {
        Theme theme;
        scheme = scheme.toLowerCase(Locale.ROOT);
        try {
            switch (scheme) {
                case "retro":
                case "light":
                    theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
                    theme.apply(this);
                    break;
                case "dark":
                    theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
                    theme.apply(this);
                    break;
                default:
                    // Default to light theme if scheme is unrecognized.
                    theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
                    theme.apply(this);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}