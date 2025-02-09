package com.github.an0nn30.vim.handlers;

import com.github.an0nn30.ui.VimTextArea;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;

public class NormalModeShortcutHandler extends VimModeShortcutHandler {

    public NormalModeShortcutHandler(VimTextArea editor) {
        super(editor);
    }

    @Override
    public boolean handleKeyPressed(KeyEvent e) {
        char keyChar = e.getKeyChar();
        switch (keyChar) {
            case 'h':
                editor.moveLeft();
                return true;
            case 'j':
                editor.moveDown();
                return true;
            case 'k':
                editor.moveUp();
                return true;
            case 'l':
                editor.moveRight();
                return true;
            case 'w': // next small word
                moveToNextWord(false);
                return true;
            case 'W': // next big word
                moveToNextWord(true);
                return true;
            case 'b': // previous small word
                moveToPreviousWord(false);
                return true;
            case 'B': // previous big word
                moveToPreviousWord(true);
                return true;
            default:
                return false;
        }
    }

    /**
     * Moves the caret to the beginning of the next word.
     * For big words (when bigWord is true), a word is any non-whitespace sequence.
     * For small words, an alphanumeric word is a sequence of letters, digits, or underscores,
     * and a punctuation word is any contiguous sequence of non-whitespace characters
     * that are not alphanumeric.
     *
     * @param bigWord if true, treat any non-whitespace as part of a word,
     *                otherwise use the above small-word rules.
     */
    private void moveToNextWord(boolean bigWord) {
        try {
            int pos = editor.getCaretPosition();
            int docLength = editor.getDocument().getLength();
            if (pos >= docLength) {
                return;
            }
            // Get text from the current position to the end of the document.
            String text = editor.getDocument().getText(pos, docLength - pos);
            int offset = 0;
            // Skip any whitespace at the current caret position.
            while (offset < text.length() && Character.isWhitespace(text.charAt(offset))) {
                offset++;
            }
            // If we reached the end, set the caret and return.
            if (offset >= text.length()) {
                editor.setCaretPosition(pos + offset);
                return;
            }
            if (bigWord) {
                // Big-word motion: skip over all non-whitespace characters.
                while (offset < text.length() && !Character.isWhitespace(text.charAt(offset))) {
                    offset++;
                }
            } else {
                // Small-word motion: determine the type of token (alphanumeric or punctuation).
                char current = text.charAt(offset);
                boolean inAlpha = Character.isLetterOrDigit(current) || current == '_';
                // Skip over characters that belong to the same token.
                if (inAlpha) {
                    while (offset < text.length() &&
                            (Character.isLetterOrDigit(text.charAt(offset)) || text.charAt(offset) == '_')) {
                        offset++;
                    }
                } else {
                    // For punctuation, skip until we hit whitespace or an alphanumeric character.
                    while (offset < text.length() &&
                            !Character.isWhitespace(text.charAt(offset)) &&
                            !(Character.isLetterOrDigit(text.charAt(offset)) || text.charAt(offset) == '_')) {
                        offset++;
                    }
                }
            }
            // Finally, skip any whitespace after the token so we land at the start of the next word.
            while (offset < text.length() && Character.isWhitespace(text.charAt(offset))) {
                offset++;
            }
            editor.setCaretPosition(pos + offset);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Moves the caret to the beginning of the previous word, traversing into
     * earlier lines if necessary.
     *
     * @param bigWord if true, use whitespace as the delimiter; else, use letters/digits/underscore.
     */
    private void moveToPreviousWord(boolean bigWord) {
        try {
            int pos = editor.getCaretPosition();
            if (pos <= 0) {
                return;
            }
            // Get the full document text.
            String fullText = editor.getDocument().getText(0, editor.getDocument().getLength());
            // Step one character left to handle the case when the caret is already at a word boundary.
            pos--;
            if (bigWord) {
                // Skip backwards over any whitespace.
                while (pos > 0 && Character.isWhitespace(fullText.charAt(pos))) {
                    pos--;
                }
                // Then, move backwards over non-whitespace.
                while (pos > 0 && !Character.isWhitespace(fullText.charAt(pos - 1))) {
                    pos--;
                }
            } else {
                // Skip backwards over non-word characters.
                while (pos > 0 && !isWordChar(fullText.charAt(pos))) {
                    pos--;
                }
                // Then, move backwards over word characters.
                while (pos > 0 && isWordChar(fullText.charAt(pos - 1))) {
                    pos--;
                }
            }
            editor.setCaretPosition(pos);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns true if the character is considered part of a “small word”
     * (letters, digits, or underscore).
     */
    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}