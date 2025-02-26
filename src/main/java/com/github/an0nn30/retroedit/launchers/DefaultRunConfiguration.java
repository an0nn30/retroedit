package com.github.an0nn30.retroedit.launchers;

/**
 * An interface for built‑in (default) run configurations.
 */
public interface DefaultRunConfiguration {
    /**
     * Returns the name of the default configuration.
     *
     * @return the name.
     */
    String getName();

    /**
     * Executes the configuration.
     * In a real implementation, this would invoke the terminal widget,
     * run the appropriate commands, parse pom.xml (if needed), etc.
     */
    void execute();
}
