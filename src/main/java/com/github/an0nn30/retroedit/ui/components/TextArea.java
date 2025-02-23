package com.github.an0nn30.retroedit.ui.components;

import com.github.an0nn30.retroedit.logging.Logger;
import com.github.an0nn30.retroedit.settings.Settings;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import com.github.an0nn30.retroedit.settings.Constants;
import com.github.an0nn30.retroedit.ui.EditorFrame;
import org.fife.ui.rsyntaxtextarea.Theme;

import java.io.File;

public class TextArea extends RSyntaxTextArea {

    private File activeFile;

    public TextArea(EditorFrame editorFrame) {
        super();
        // Initially, no file is associated so default to none.
        setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        applyTheme(Settings.getInstance().getInterfaceTheme());
    }

    /**
     * Sets the active file and updates the syntax style accordingly.
     */
    public void setActiveFile(File file) {
        this.activeFile = file;
        updateSyntaxStyle();
    }

    public File getActiveFile() {
        return activeFile;
    }

    /**
     * Updates the syntax style based on the file extension.
     */
    private void updateSyntaxStyle() {
        String syntax;
        if (activeFile != null) {
            String extension = getFileExtension(activeFile);
            // Look up the syntax style in your supported file types map.
            syntax = Constants.supportedFileTypes.getOrDefault(extension, SyntaxConstants.SYNTAX_STYLE_NONE);
        } else {
            syntax = SyntaxConstants.SYNTAX_STYLE_NONE;
        }
        setSyntaxEditingStyle(syntax);
    }

    /**
     * Returns the file extension for the given file.
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < name.length() - 1) {
            return name.substring(dotIndex + 1);
        }
        return "";
    }

    public void initFontSizeAndFamily() {
        Logger.getInstance().info(TextArea.class, "Settings font size and family: " + Settings.getInstance().getEditorFontSize() + " " + Settings.getInstance().getEditorFontFamily());
        setFont(new java.awt.Font(Settings.getInstance().getEditorFontFamily(), java.awt.Font.PLAIN, Settings.getInstance().getEditorFontSize()));
    }

    private void applyTheme(String scheme) {
        Theme theme;
        try {
            switch (scheme) {
                case "Light":
                case "Retro":
                    theme = Theme.load(TextArea.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
                    theme.apply(this);
                    break;
                case "Dark":
                    theme = Theme.load(TextArea.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
                    theme.apply(this);
                    break;
                default:
                    theme = Theme.load(TextArea.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}