package com.github.an0nn30.retroedit.ui.components;

import javax.swing.*;

public class Button extends JButton {

    private String buttonName;

    public Button(Icon icon, String buttonName) {
        super(icon);
        this.buttonName = buttonName;
    }

    public String getButtonName() {
        return buttonName;
    }

    public void setButtonName(String buttonName) {
        this.buttonName = buttonName;
    }
}
