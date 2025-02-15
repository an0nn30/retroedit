package com.github.an0nn30.editor.ui.components;

import com.github.an0nn30.editor.event.EventRecord;
import com.github.an0nn30.editor.event.EventBus;
import com.github.an0nn30.editor.event.EventType;
import com.github.an0nn30.editor.logging.Logger;
import com.github.an0nn30.editor.settings.Settings;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import com.github.an0nn30.editor.ui.Editor;
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
}
