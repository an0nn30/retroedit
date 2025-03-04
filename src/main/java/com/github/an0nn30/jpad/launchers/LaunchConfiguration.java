package com.github.an0nn30.jpad.launchers;

// LaunchConfiguration.java
import com.github.an0nn30.jpad.event.EventBus;
import com.github.an0nn30.jpad.event.EventType;

import java.util.List;
import java.util.Map;

public class LaunchConfiguration {
    private String name;
    private String command;
    private List<String> args;
    private Map<String, String> env;

    // Default constructor needed for Jackson
    public LaunchConfiguration() {}

    public LaunchConfiguration(String name, String command, List<String> args, Map<String, String> env) {
        this.name = name;
        this.command = command;
        this.args = args;
        this.env = env;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    /**
     * Executes the launch configuration.
     * Currently, this is a dummy implementation that just prints out the details.
     */
    public void execute() {
        EventBus.publish(EventType.PROCESS_RUNNING.name(), this.name, this);
        System.out.println("Executing configuration: " + name);
        System.out.println("Command: " + command);
        System.out.println("Arguments: " + args);
        if (env != null && !env.isEmpty()) {
            System.out.println("Environment: " + env);
        }
        EventBus.publish(EventType.PROCESS_STOPPED.name(), this.name, this);
        // Dummy call to a Terminal widget execution method:
        // TerminalWidget.execute(command, args, env);
    }
}
