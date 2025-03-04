package com.github.an0nn30.jpad.settings;


import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final Map<String, String> supportedFileTypes = new HashMap<>();

    static {
        supportedFileTypes.put("java", SyntaxConstants.SYNTAX_STYLE_JAVA);
        supportedFileTypes.put("py", SyntaxConstants.SYNTAX_STYLE_PYTHON);
        supportedFileTypes.put("c", SyntaxConstants.SYNTAX_STYLE_C);
        supportedFileTypes.put("cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
        supportedFileTypes.put("json", SyntaxConstants.SYNTAX_STYLE_JSON);
        supportedFileTypes.put("go", SyntaxConstants.SYNTAX_STYLE_GO);
        supportedFileTypes.put("yaml", SyntaxConstants.SYNTAX_STYLE_YAML);
    }
}
