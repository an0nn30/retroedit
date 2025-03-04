package com.github.an0nn30.jpad.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A custom flat button that appears without borders or content fill by default,
 * and shows a subtle light hint when the mouse hovers over it.
 */
public class Button extends JButton {
    private final Color originalBackground = getBackground();


    /**
     * Constructs a new Button with the specified icon.
     *
     * @param icon       the icon to display on the button.
     */
    public Button(Icon icon) {
        super(icon);
        initialize();
    }

    /**
     *  Constructs a new Button with the specific icon, and insets.
     */
    public Button(Icon icon, Insets insets) {
        super(icon);
        setMargin(insets);
        initialize();
        setBackground(originalBackground);
    }

    /***
     * Contructs a new Button with specific text and insets
     * @param text
     * @param insets
     */
    public Button(String text, Insets insets) {
        super(text);
        setMargin(insets);
    }


    /**
     * Initializes the button's flat appearance and hover effect.
     */
    private void initialize() {
        // Remove default border, focus painting, and content fill for a flat look.
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);

        // Add a mouse listener to provide a light hover hint.
        addMouseListener(new MouseAdapter() {
//            private final Color originalBackground = getBackground();

            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    // On hover, enable content fill and set a light gray background.
                    setContentAreaFilled(true);
                    setBackground(originalBackground.darker()); // Light gray hint color.
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    // When the mouse exits, revert to the flat appearance.
                    setContentAreaFilled(false);
                    setBackground(originalBackground);
                }
            }
        });
    }
}