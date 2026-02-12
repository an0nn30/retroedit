package com.github.an0nn30.jpad.project;

import com.github.an0nn30.jpad.event.EventBus;
import com.github.an0nn30.jpad.event.EventType;
import com.github.an0nn30.jpad.logging.Logger;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ProjectManager {

    private volatile JavaProject currentProject;

    public CompletableFuture<Optional<JavaProject>> loadProject(File rootDir) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<JavaProject> detected = ProjectDetector.detect(rootDir);
            if (detected.isPresent()) {
                JavaProject project = detected.get();
                try {
                    project.resolveDependencies();
                    currentProject = project;
                    Logger.getInstance().info(ProjectManager.class,
                            "Project loaded: " + project.getBuildSystem() + " at " + rootDir.getAbsolutePath());
                    EventBus.publish(EventType.PROJECT_LOADED.name(), project, this);
                    return Optional.of(project);
                } catch (Exception e) {
                    Logger.getInstance().error(ProjectManager.class,
                            "Failed to resolve project: " + e.getMessage());
                    EventBus.publish(EventType.PROJECT_RESOLUTION_FAILED.name(), e.getMessage(), this);
                    // Still set the project reference even if resolution failed
                    currentProject = project;
                    return Optional.of(project);
                }
            }
            return Optional.empty();
        });
    }

    public JavaProject getCurrentProject() {
        return currentProject;
    }
}
