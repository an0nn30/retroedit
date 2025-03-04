package com.github.an0nn30.jpad.autocomplete;

import com.github.an0nn30.jpad.autocomplete.java.JarFileFinder;
import com.github.an0nn30.jpad.ui.TextAreaTabManager;
import com.github.an0nn30.jpad.ui.components.DirectoryTree;
import org.fife.rsta.ac.AbstractSourceTree;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rsta.ac.java.tree.JavaOutlineTree;
import org.fife.rsta.ac.js.tree.JavaScriptOutlineTree;
import org.fife.rsta.ac.xml.tree.XmlOutlineTree;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.Set;

public class SourceTreeRefresher {

    private final TextAreaTabManager textAreaTabManager;
    private final DirectoryTree directoryTree;
    private final JScrollPane treeSP;
    private final LanguageSupportFactory lsf;
    private AbstractSourceTree sourceTree;

    public SourceTreeRefresher(TextAreaTabManager textAreaTabManager, DirectoryTree directoryTree, JScrollPane treeSP) {
        this.textAreaTabManager = textAreaTabManager;
        this.directoryTree = directoryTree;
        this.treeSP = treeSP;
        this.lsf = LanguageSupportFactory.get();
    }

    public void refresh() {
        if (sourceTree != null) {
            sourceTree.uninstall();
        }

        String language = textAreaTabManager.getActiveTextArea().getSyntaxEditingStyle();

        if (SyntaxConstants.SYNTAX_STYLE_JAVA.equals(language)) {
            sourceTree = new JavaOutlineTree();
            LanguageSupport support = lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVA);
            JavaLanguageSupport jls = (JavaLanguageSupport) support;
            support.setAutoActivationEnabled(true);
            support.setAutoActivationDelay(0);
            support.install(textAreaTabManager.getActiveTextArea());

            try {
                // Determine the starting directory: prefer directoryTree.getRootDirectory() if available.
                File currentDir = (directoryTree != null && directoryTree.getRootDirectory() != null)
                        ? directoryTree.getRootDirectory()
                        : new File(System.getProperty("user.dir"));

                JarFileFinder jarFinder = new JarFileFinder();
                Set<File> jarFiles = jarFinder.findJarFiles(currentDir);

                for (File file : jarFiles) {
                    jls.getJarManager().addClassFileSource(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            jls.install(textAreaTabManager.getActiveTextArea());
        } else if (SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT.equals(language)) {
            sourceTree = new JavaScriptOutlineTree();
        } else if (SyntaxConstants.SYNTAX_STYLE_XML.equals(language)) {
            sourceTree = new XmlOutlineTree();
        } else {
            sourceTree = null;
        }

        if (sourceTree != null) {
            sourceTree.listenTo(textAreaTabManager.getActiveTextArea());
            treeSP.setViewportView(sourceTree);
        } else {
            JTree dummy = new JTree((TreeNode) null);
            treeSP.setViewportView(dummy);
        }
        treeSP.revalidate();
    }
}