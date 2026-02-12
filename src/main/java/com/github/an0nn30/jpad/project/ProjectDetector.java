package com.github.an0nn30.jpad.project;

import java.io.File;
import java.util.Optional;

public class ProjectDetector {

    public static Optional<JavaProject> detect(File rootDir) {
        if (rootDir == null || !rootDir.isDirectory()) {
            return Optional.empty();
        }

        if (new File(rootDir, "pom.xml").exists()) {
            return Optional.of(new MavenProject(rootDir));
        }

        if (new File(rootDir, "build.gradle").exists()
                || new File(rootDir, "build.gradle.kts").exists()
                || new File(rootDir, "settings.gradle").exists()
                || new File(rootDir, "settings.gradle.kts").exists()) {
            return Optional.of(new GradleProject(rootDir));
        }

        return Optional.empty();
    }
}
