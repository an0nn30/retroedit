package com.github.an0nn30.retroedit.logging;


import com.github.an0nn30.retroedit.settings.Settings;

public class Logger {

    public enum LogLevel {
        DEBUG(1), INFO(2), WARN(3), ERROR(4);

        private final int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    // Configured log level loaded from Settings
    private LogLevel configuredLevel;

    // Private constructor: load configuration from Settings
    private Logger() {
        // Assume Settings has a logLevel property as a String (e.g., "DEBUG", "INFO", etc.)
        String levelStr = Settings.getInstance().getLogLevel();
        try {
            configuredLevel = LogLevel.valueOf(levelStr.toUpperCase());
        } catch (Exception e) {
            configuredLevel = LogLevel.INFO;
        }
    }

    // Initialization-on-demand holder idiom for thread-safe lazy singleton.
    private static class LoggerHolder {
        private static final Logger INSTANCE = new Logger();
    }

    public static Logger getInstance() {
        return LoggerHolder.INSTANCE;
    }

    // Logging methods that require the caller to pass in its class.

    public void debug(Class<?> clazz, String message) {
        if (configuredLevel.getLevel() <= LogLevel.DEBUG.getLevel()) {
            log("DEBUG", clazz, message);
        }
    }

    public void info(Class<?> clazz, String message) {
        if (configuredLevel.getLevel() <= LogLevel.INFO.getLevel()) {
            log("INFO", clazz, message);
        }
    }

    public void warn(Class<?> clazz, String message) {
        if (configuredLevel.getLevel() <= LogLevel.WARN.getLevel()) {
            log("WARN", clazz, message);
        }
    }

    public void error(Class<?> clazz, String message) {
        if (configuredLevel.getLevel() <= LogLevel.ERROR.getLevel()) {
            log("ERROR", clazz, message);
        }
    }

    // Internal method to format and print log messages.
    private void log(String level, Class<?> clazz, String message) {
        String logMessage = String.format("[%s] %s - %s", level, clazz.getName(), message);
        System.out.println(logMessage);
    }
}
