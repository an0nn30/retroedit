package com.github.an0nn30.editor.ui;

import com.formdev.flatlaf.*;
import com.github.an0nn30.editor.event.EventRecord;
import com.github.an0nn30.editor.event.EventBus;
import com.github.an0nn30.editor.event.EventType;
import com.github.an0nn30.editor.logging.Logger;
import com.github.an0nn30.editor.settings.Settings;
import com.github.an0nn30.editor.ui.components.TextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.awt.*;

public class Editor extends JFrame {

    private MainToolbar mainToolbar;
    private TabManager tabManager;
    private StatusPanel statusPanel;

    public Editor() {
        super("Editor");
        FlatIntelliJLaf.install();
        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create top-level components with this Editor as reference.
        mainToolbar = new MainToolbar(this);
        tabManager = new TabManager(this);
        statusPanel = new StatusPanel();

        setJMenuBar(new MenuBar(this).getMenuBar());


        add(mainToolbar, BorderLayout.NORTH);
        add(tabManager, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Start with one untitled tab.
        tabManager.addNewTab("Untitled", new TextArea(this));

        updateInterfaceTheme(null);
        EventBus.subscribe(EventType.TAB_UPDATED.name(), (EventRecord<Object> eventRecord) -> {
            String title = eventRecord.data().toString();
            setTitle(title);
        });
        EventBus.subscribe(EventType.THEME_CHANGED.name(), (EventRecord<Object> eventRecord) -> updateInterfaceTheme(eventRecord));
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    private void updateInterfaceTheme(EventRecord<Object> eventRecord) {
        String theme = eventRecord == null ? Settings.getInstance().getInterfaceTheme() : eventRecord.data().toString();

        var textArea = tabManager.getActiveTextArea();

        try {
            if (theme.equalsIgnoreCase("light")) {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
                Theme textAreaTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
                textAreaTheme.apply(textArea);

            } else {
                UIManager.setLookAndFeel(new FlatDarculaLaf());
                Theme textAreaTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
                textAreaTheme.apply(textArea);

            }
        } catch (Exception e) {
            Logger.getInstance().error(StatusPanel.class, "Failed to set FlatDarkLaf: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Recursively update the UI of all components starting with this container.
        SwingUtilities.updateComponentTreeUI(this);

        // Revalidate and repaint to ensure the changes are visible immediately.
        revalidate();
        repaint();
    }
}
