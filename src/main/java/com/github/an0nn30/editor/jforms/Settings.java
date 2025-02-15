package com.github.an0nn30.editor.jforms;

import com.github.an0nn30.editor.ui.Editor;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Settings extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabbedPane1;
    private JComboBox colorThemeComboBox;
    private boolean themeChanged = false;
    private JSpinner fontSizeSpinner;
    private boolean fontSizeChanged = false;
    private JComboBox fontFamilySpinner;
    private boolean fontFamilyChanged = false;

    private Editor editor;

    public Settings(Editor editor) {
        this.editor = editor;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });



        colorThemeComboBox.addItem("Light");
        colorThemeComboBox.addItem("Dark");
        colorThemeComboBox.setSelectedItem(com.github.an0nn30.editor.settings.Settings.getInstance().getInterfaceTheme());
        colorThemeComboBox.addActionListener(e -> {

        });

//        editorSchemeComboBox.addItem("default");
//        editorSchemeComboBox.addItem("dark");
//        editorSchemeComboBox.addItem("idea");
//        editorSchemeComboBox.addItem("monokai");
//        editorSchemeComboBox.setSelectedItem(com.github.an0nn30.editor.settings.Settings.getInstance().getEditorColorScheme());

        Integer[] fontSizes = {8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36};
        fontSizeSpinner.setModel(new SpinnerListModel(fontSizes));
        fontSizeSpinner.setValue(com.github.an0nn30.editor.settings.Settings.getInstance().getEditorFontSize());

        String[] fontFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String fontFamily : fontFamilies) {
            fontFamilySpinner.addItem(fontFamily);
        }
        fontFamilySpinner.setSelectedItem(com.github.an0nn30.editor.settings.Settings.getInstance().getEditorFontFamily());


        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        var settings = com.github.an0nn30.editor.settings.Settings.getInstance();
        settings.setEditorFontSize((int) fontSizeSpinner.getValue());
        settings.setInterfaceTheme(colorThemeComboBox.getSelectedItem().toString());
//        settings.setEditorColorScheme(editorSchemeComboBox.getSelectedItem().toString());
        settings.setEditorFontFamily(fontFamilySpinner.getSelectedItem().toString());

        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
