package com.github.an0nn30.vim.handlers;

import com.github.an0nn30.ui.VimTextArea;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;

public class NormalModeShortcutHandler extends VimModeShortcutHandler {

    // Used to detect a multi-key sequence ("gg").
    private boolean awaitingG = false;

    public NormalModeShortcutHandler(VimTextArea editor) {
        super(editor);
    }

    @Override
    public boolean handleKeyPressed(KeyEvent e) {
        char keyChar = e.getKeyChar();

        // If we are waiting for a second 'g' but receive a key that is not 'g',
        // then clear the waiting state.
        if (awaitingG && keyChar != 'g') {
            awaitingG = false;
        }

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
            case 'w':
                moveToNextWord(false);
                return true;
            case 'W':
                moveToNextWord(true);
                return true;
            case 'b':
                moveToPreviousWord(false);
                return true;
            case 'B':
                moveToPreviousWord(true);
                return true;
            case 'G':  // Jump to the bottom of the document.
                moveToBottom();
                return true;
            case 'g':
                if (awaitingG) {
                    // Second 'g' detected ("gg"): jump to the top of the document.
                    moveToTop();
                    awaitingG = false;
                    return true;
                } else {
                    // First 'g': set the waiting flag and consume the event.
                    awaitingG = true;
                    return true;
                }
            default:
                return false;
        }
    }

    /**
     * Moves the caret to the very top (beginning) of the document.
     */
    private void moveToTop() {
        editor.setCaretPosition(0);
    }

    /**
     * Moves the caret to the very bottom (end) of the document.
     */
    private void moveToBottom() {
        int docLength = editor.getDocument().getLength();
        editor.setCaretPosition(docLength);
    }

    /**
     * Moves the caret to the beginning of the next word.
     *
     * For big words (when bigWord is true), a word is any non-whitespace sequence.
     * For small words, an alphanumeric word is a sequence of letters, digits, or underscores,
     * while punctuation is treated as its own word.
     *
     * @param bigWord if true, treat any non-whitespace as part of a word;
     *                otherwise, use small-word rules.
     */
    private void moveToNextWord(boolean bigWord) {
        try {
            int pos = editor.getCaretPosition();
            int docLength = editor.getDocument().getLength();
            if (pos >= docLength) {
                return;
            }
            // Get the text from the current position to the end of the document.
            String text = editor.getDocument().getText(pos, docLength - pos);
            int offset = 0;
            // Skip any initial whitespace.
            while (offset < text.length() && Character.isWhitespace(text.charAt(offset))) {
                offset++;
            }
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
                // Determine the token type of the next word.
                char current = text.charAt(offset);
                boolean inAlpha = Character.isLetterOrDigit(current) || current == '_';
                if (inAlpha) {
                    // Skip over contiguous alphanumeric (or underscore) characters.
                    while (offset < text.length() &&
                            (Character.isLetterOrDigit(text.charAt(offset)) || text.charAt(offset) == '_')) {
                        offset++;
                    }
                } else {
                    // For punctuation, skip over contiguous characters that are neither whitespace nor alphanumeric.
                    while (offset < text.length() &&
                            !Character.isWhitespace(text.charAt(offset)) &&
                            !(Character.isLetterOrDigit(text.charAt(offset)) || text.charAt(offset) == '_')) {
                        offset++;
                    }
                }
            }
            // Finally, skip any whitespace to land at the beginning of the next word.
            while (offset < text.length() && Character.isWhitespace(text.charAt(offset))) {
                offset++;
            }
            editor.setCaretPosition(pos + offset);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Moves the caret to the beginning of the previous word.
     * This method traverses across line boundaries.
     *
     * @param bigWord if true, use whitespace as the delimiter;
     *                otherwise, consider letters/digits/underscore as word characters.
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
     * Returns true if the character is considered part of a small word
     * (i.e. letters, digits, or underscore).
     */
    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}