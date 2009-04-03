package com.intellij.tapestry.intellij.core.log;

import com.intellij.idea.LoggerFactory;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.tapestry.core.log.Logger;

public class IntellijLogger implements Logger {

    private com.intellij.openapi.diagnostic.Logger _logger;

    public IntellijLogger(String loggerName) {
        _logger = LoggerFactory.getInstance().getLoggerInstance(loggerName);
    }

    public void debug(String message) {
        _logger.debug(message);
    }

    public void debug(Throwable exception) {
        _logger.debug(exception);
    }

    public void debug(String message, Throwable exception) {
        _logger.debug(message, exception);
    }

    public void info(String message) {
        _logger.info(message);
    }

    public void info(Throwable exception) {
        _logger.info(exception);
    }

    public void info(String message, Throwable exception) {
        _logger.info(message, exception);
    }

    public void warn(String message) {
        _logger.warn(message);
    }

    public void warn(Throwable exception) {
        _logger.warn(exception);
    }

    public void warn(String message, Throwable exception) {
        _logger.warn(message, exception);
    }

    public void error(String message) {
        _logger.error(message);
    }

    public void error(Throwable exception) {
        // ignore ProcessCanceledException
        if (exception instanceof ProcessCanceledException)
            return;

        try {
            _logger.error(exception);
        } catch (ProcessCanceledException ex) {
            // ignore
        }
    }

    public void error(String message, Throwable exception) {
        try {
            _logger.error(message, exception);
        } catch (ProcessCanceledException ex) {
            // ignore
        }
    }
}
