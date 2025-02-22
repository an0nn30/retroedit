package com.github.an0nn30.retroedit.logging;

import com.github.an0nn30.retroedit.settings.Settings;

/**
 * Logger is a simple logging utility that prints log messages to the console.
 * It supports four log levels (DEBUG, INFO, WARN, ERROR) and uses a lazy-loaded
 * singleton instance. The configured log level is loaded from application Settings.
 */
public class Logger {

    /**
     * Enumeration of log levels with associated numeric values.
     */
    public enum LogLevel {
        DEBUG(1), INFO(2), WARN(3), ERROR(4);

        private final int level;

        /**
         * Constructs a LogLevel with the specified numeric value.
         *
         * @param level the numeric value associated with the log level.
         */
        LogLevel(int level) {
            this.level = level;
        }

        /**
         * Returns the numeric value of the log level.
         *
         * @return the numeric level.
         */
        public int getLevel() {
            return level;
        }
    }

    // Configured log level loaded from Settings.
    private LogLevel configuredLevel;

    /**
     * Private constructor that initializes the Logger by reading the log level from Settings.
     */
    private Logger() {
        String levelStr = Settings.getInstance().getLogLevel();
        try {
            configuredLevel = LogLevel.valueOf(levelStr.toUpperCase());
        } catch (Exception e) {
            configuredLevel = LogLevel.INFO; // Fallback to INFO if parsing fails.
        }
    }

    /**
     * Initialization-on-demand holder idiom for thread-safe lazy singleton instance.
     */
    private static class LoggerHolder {
        private static final Logger INSTANCE = new Logger();
    }

    /**
     * Returns the singleton instance of Logger.
     *
     * @return the Logger instance.
     */
    public static Logger getInstance() {
        return LoggerHolder.INSTANCE;
    }

    /**
     * Logs a DEBUG level message if the DEBUG level is enabled.
     *
     * @param clazz   the class from which the log originates.
     * @param message the message to log.
     */
    public void debug(Class<?> clazz, String message) {
        if (isLogLevelEnabled(LogLevel.DEBUG)) {
            log("DEBUG", clazz, message);
        }
    }

    /**
     * Logs an INFO level message if the INFO level is enabled.
     *
     * @param clazz   the class from which the log originates.
     * @param message the message to log.
     */
    public void info(Class<?> clazz, String message) {
        if (isLogLevelEnabled(LogLevel.INFO)) {
            log("INFO", clazz, message);
        }
    }

    /**
     * Logs a WARN level message if the WARN level is enabled.
     *
     * @param clazz   the class from which the log originates.
     * @param message the message to log.
     */
    public void warn(Class<?> clazz, String message) {
        if (isLogLevelEnabled(LogLevel.WARN)) {
            log("WARN", clazz, message);
        }
    }

    /**
     * Logs an ERROR level message if the ERROR level is enabled.
     *
     * @param clazz   the class from which the log originates.
     * @param message the message to log.
     */
    public void error(Class<?> clazz, String message) {
        if (isLogLevelEnabled(LogLevel.ERROR)) {
            log("ERROR", clazz, message);
        }
    }

    /**
     * Determines if a given log level is enabled based on the configured log level.
     *
     * @param level the log level to check.
     * @return true if the provided level should be logged; false otherwise.
     */
    private boolean isLogLevelEnabled(LogLevel level) {
        return configuredLevel.getLevel() <= level.getLevel();
    }

    /**
     * Internal method to format and print the log message to the console.
     *
     * @param level   the log level label.
     * @param clazz   the class from which the message originates.
     * @param message the log message.
     */
    private void log(String level, Class<?> clazz, String message) {
        String logMessage = String.format("[%s] %s - %s", level, clazz.getName(), message);
        System.out.println(logMessage);
    }
}