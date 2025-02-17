package com.github.an0nn30.retroedit.ui;

import com.jediterm.terminal.ui.JediTermWidget;
import javax.swing.*;

public class SplitPane extends JSplitPane {
    JediTermWidget widget;
    TabManager tabManager;
    boolean enabled = false;
    public SplitPane(TabManager tabManager, JediTermWidget widget) {
        super(JSplitPane.VERTICAL_SPLIT);
        this.widget = widget;
        this.tabManager = tabManager;
        setDividerLocation(0.5);
        setContinuousLayout(true);
        setOneTouchExpandable(true);
        setTopComponent(tabManager);
        add(widget);
        setResizeWeight(1.0);
    }

    public void toggleTerminal() {
        enabled = !enabled;
        System.out.println("toggleTerminal: " + enabled);
        setBottomComponent(enabled ? this.widget : null);
        if (enabled) {
            this.widget.requestFocus();
        } else {
            this.tabManager.requestFocus();
        }
    }
}
