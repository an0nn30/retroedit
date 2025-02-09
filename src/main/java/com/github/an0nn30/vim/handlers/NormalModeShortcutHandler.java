package com.github.an0nn30.vim.handlers;

import com.github.an0nn30.ui.VimTextArea;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;

public class NormalModeShortcutHandler extends VimModeShortcutHandler {

    // For multi-key sequence "gg" (jump to top).
    private boolean awaitingG = false;

    // For collecting digits after a colon.
    private StringBuilder commandBuffer = null;

    public NormalModeShortcutHandler(VimTextArea editor) {
        super(editor);
    }

    @Override
    public boolean handleKeyPressed(KeyEvent e) {
        char keyChar = e.getKeyChar();

        // If we are in command mode (after ':'), process numeric input.
        if (commandBuffer != null) {
            // Accept digits.
            if (Character.isDigit(keyChar)) {
                commandBuffer.append(keyChar);
                e.consume();
                return true;
            }
            // Execute the command when Enter is pressed.
            else if (keyChar == KeyEvent.VK_ENTER || keyChar == '\n') {
                executeLineJump();
                e.consume();
                return true;
            }
            // If a non-digit (other than Enter) is pressed,
            // then if there is a number, execute the command.
            else {
                if (commandBuffer.length() > 0) {
                    executeLineJump();
                    // Let the non-digit key be processed normally.
                } else {
                    // No digits were entered; simply cancel.
                    commandBuffer = null;
                }
                // Continue processing the current key below.
            }
        }

        // Start command mode if ':' is pressed (and we're not already capturing a command).
        if (commandBuffer == null && keyChar == ':') {
            commandBuffer = new StringBuilder();
            e.consume();
            return true;
        }

        // If we are waiting for the second 'g' for "gg", clear the waiting flag if the key isn't 'g'.
        if (awaitingG && keyChar != 'g') {
            awaitingG = false;
        }

        // Process other NORMAL-mode commands.
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
            case 'G':
                moveToBottom();
                return true;
            case 'g':
                if (awaitingG) {
                    // "gg": jump to the top.
                    moveToTop();
                    awaitingG = false;
                    return true;
                } else {
                    awaitingG = true;
                    return true;
                }
            default:
                return false;
        }
    }

    /**
     * Parses the accumulated number and jumps the caret to the beginning of that line.
     * Lines are 1-indexed (as in Vim), so line 1 is the top of the document.
     */
    private void executeLineJump() {
        if (commandBuffer != null && commandBuffer.length() > 0) {
            try {
                int requestedLine = Integer.parseInt(commandBuffer.toString());
                int lineCount = editor.getLineCount();
                // Clamp the line number to a valid range.
                if (requestedLine < 1) {
                    requestedLine = 1;
                } else if (requestedLine > lineCount) {
                    requestedLine = lineCount;
                }
                // Convert 1-indexed to 0-indexed.
                int lineStartOffset = editor.getLineStartOffset(requestedLine - 1);
                editor.setCaretPosition(lineStartOffset);
            } catch (BadLocationException | NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        commandBuffer = null;
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
            String text = editor.getDocument().getText(pos, docLength - pos);
            int offset = 0;
            // Skip any whitespace.
            while (offset < text.length() && Character.isWhitespace(text.charAt(offset))) {
                offset++;
            }
            if (offset >= text.length()) {
                editor.setCaretPosition(pos + offset);
                return;
            }
            if (bigWord) {
                while (offset < text.length() && !Character.isWhitespace(text.charAt(offset))) {
                    offset++;
                }
            } else {
                char current = text.charAt(offset);
                boolean inAlpha = Character.isLetterOrDigit(current) || current == '_';
                if (inAlpha) {
                    while (offset < text.length() &&
                            (Character.isLetterOrDigit(text.charAt(offset)) || text.charAt(offset) == '_')) {
                        offset++;
                    }
                } else {
                    while (offset < text.length() &&
                            !Character.isWhitespace(text.charAt(offset)) &&
                            (!Character.isLetterOrDigit(text.charAt(offset)) && text.charAt(offset) != '_')) {
                        offset++;
                    }
                }
            }
            // Skip trailing whitespace.
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
     * earlier parts of the document if necessary.
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
            String fullText = editor.getDocument().getText(0, editor.getDocument().getLength());
            pos--;  // Step one character left to handle word boundaries.
            if (bigWord) {
                while (pos > 0 && Character.isWhitespace(fullText.charAt(pos))) {
                    pos--;
                }
                while (pos > 0 && !Character.isWhitespace(fullText.charAt(pos - 1))) {
                    pos--;
                }
            } else {
                while (pos > 0 && !isWordChar(fullText.charAt(pos))) {
                    pos--;
                }
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
     * (letters, digits, or underscore).
     */
    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}