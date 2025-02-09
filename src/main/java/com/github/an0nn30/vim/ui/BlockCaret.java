package com.github.an0nn30.vim.ui;


import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class BlockCaret extends DefaultCaret {
    @Override
    public void paint(Graphics g) {
        if (isVisible()) {
            try {
                JTextComponent comp = getComponent();
                int dot = getDot();
                Rectangle r = comp.modelToView(dot);
                if (r == null) {
                    return;
                }
                g.setColor(comp.getCaretColor());

                // Estimate the width of a typical character if the rectangle width is zero.
                FontMetrics fm = comp.getFontMetrics(comp.getFont());
                int charWidth = (r.width > 0) ? r.width : fm.charWidth('w');

                // Fill a rectangle at the caret position, creating a block effect.
                g.fillRect(r.x, r.y, charWidth, r.height);
            } catch (BadLocationException e) {
                // Exception can be safely ignored here.
            }
        }
    }
}