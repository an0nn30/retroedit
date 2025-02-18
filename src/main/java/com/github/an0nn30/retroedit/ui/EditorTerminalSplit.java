package com.github.an0nn30.retroedit.ui;

import com.github.an0nn30.retroedit.logging.Logger;
import com.jediterm.terminal.ui.JediTermWidget;
import javax.swing.*;

public class EditorTerminalSplit extends JSplitPane {
    JediTermWidget widget;
    TabManager tabManager;
    boolean enabled = false;
    public EditorTerminalSplit(TabManager tabManager, JediTermWidget widget) {
        super(JSplitPane.VERTICAL_SPLIT);
        this.widget = widget;
        this.tabManager = tabManager;
//        setDividerLocation(0.5);
//        setContinuousLayout(true);
        setOneTouchExpandable(true);
        setTopComponent(tabManager);
        setBottomComponent(widget);
//        setResizeWeight(1.0);
    }

    public void toggleTerminal() {
        enabled = !enabled;
        Logger.getInstance().info(EditorTerminalSplit.class, "toggleTerminal: " + enabled);
        setDividerLocation(enabled ? 0.5 : 1.0);
//        System.out.println("toggleTerminal: " + enabled);
//
//        setBottomComponent(enabled ? this.widget : null);
//        if (enabled) {
//            this.widget.requestFocus();
//        } else {
//            this.tabManager.requestFocus();
//        }
    }

    public void startMinimized() {
        setDividerLocation(0.0);
    }
}
