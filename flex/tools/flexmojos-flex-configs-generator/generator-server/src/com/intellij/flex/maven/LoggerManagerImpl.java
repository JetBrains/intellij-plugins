package com.intellij.flex.maven;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.AbstractLoggerManager;
import org.codehaus.plexus.logging.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

class LoggerManagerImpl extends AbstractLoggerManager {
  private final Logger logger;

  public LoggerManagerImpl(MyOutputStream out) {
    logger = new LoggerImpl(getThreshold(), out);
  }

  @Override
  public Logger getLoggerForComponent(String s, String s1) {
    return logger;
  }

  @Override
  public void returnComponentLogger(String s, String s1) {
  }

  @Override
  public int getThreshold() {
    return Logger.LEVEL_ERROR;
  }

  @Override
  public void setThreshold(int i) {
  }

  @Override
  public void setThresholds(int i) {
  }

  @Override
  public int getActiveLoggerCount() {
    return 1;
  }

  private static class LoggerImpl extends AbstractLogger {
    private final MyOutputStream out;

    public LoggerImpl(int threshold, MyOutputStream out) {
      super(threshold, "LoggerManagerImpl");
      this.out = out;
    }

    @Override
    public void debug(String s, Throwable throwable) {

    }

    @Override
    public void info(String s, Throwable throwable) {
    }

    @Override
    public void warn(String s, Throwable throwable) {
    }

    @Override
    public void error(String s, Throwable throwable) {
      System.out.print(s);
      System.out.print(throwableToString(throwable));
      //try {
      //  out.enable();
      //  out.write(GeneratorServer.ERROR);
      //  out.writeUTF(s);
      //  out.writeUTF(throwableToString(throwable));
      //}
      //catch (IOException ignored) {
      //}
      //finally {
      //  out.disable();
      //}
    }

    @Override
    public void fatalError(String s, Throwable throwable) {
      error(s, throwable);
    }

    @Override
    public Logger getChildLogger(String s) {
      return this;
    }

    private static String throwableToString(Throwable throwable) {
      if (throwable == null) {
        return "";
      }

      StringWriter stringWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(stringWriter);
      throwable.printStackTrace(writer);
      return stringWriter.getBuffer().toString();
    }
  }
}
