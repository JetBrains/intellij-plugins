package com.intellij.tapestry.core.log;

/**
 * Every IDE implementation has to call the setLoggerFactoryImplementation method once to 
 * provide the correct logger factory implementation.
 */
public abstract class LoggerFactory {

    private static LoggerFactory _loggerFactory;

    public static void setLoggerFactoryImplementation(LoggerFactory loggerFactory) throws LoggerFactoryAlreadySetException {
        if (_loggerFactory != null)
            throw new LoggerFactoryAlreadySetException();

        _loggerFactory = loggerFactory;
    }//setLoggerFactoryImplementation

    public static LoggerFactory getInstance() {
        return _loggerFactory;
    }//getInstance

    public abstract Logger getLogger(Class clazz);

}//LoggerFactory

