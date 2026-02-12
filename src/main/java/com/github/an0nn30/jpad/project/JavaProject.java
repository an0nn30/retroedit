package com.github.an0nn30.jpad.project;

import java.io.File;
import java.util.List;

public interface JavaProject {
    File getProjectRoot();
    String getBuildSystem();
    List<File> getSourceRoots();
    List<File> getClasspath();
    File getOutputDirectory();
    String getSourceLevel();
    void resolveDependencies() throws Exception;
    List<String> getModules();
    boolean isResolved();
}
