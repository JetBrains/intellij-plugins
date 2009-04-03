package com.intellij.tapestry.core.log;

/**
 * The global plugins logger.
 */
public interface Logger {

    void debug(String message);

    void debug(Throwable exception);

    void debug(String message, Throwable exception);

    void info(String message);

    void info(Throwable exception);

    void info(String message, Throwable exception);

    void warn(String message);

    void warn(Throwable exception);

    void warn(String message, Throwable exception);

    void error(String message);

    void error(Throwable exception);

    void error(String message, Throwable exception);
}
