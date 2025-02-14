package com.github.an0nn30.editor.ui.components;

import com.github.an0nn30.editor.event.Event;
import com.github.an0nn30.editor.event.EventBus;
import com.github.an0nn30.editor.event.EventType;
import com.formdev.flatlaf.extras.FlatSVGIcon;
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
        // Subscribe only to syntax highlighting updates.
        EventBus.subscribe(EventType.SYNTAX_HIGHLIGHT_CHANGED.name(), (Event<Object> event) -> {
            String fileType = (String) event.data();
            setSyntaxEditingStyle(fileType);
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
