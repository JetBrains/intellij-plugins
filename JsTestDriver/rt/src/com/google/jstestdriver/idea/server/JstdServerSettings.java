package com.google.jstestdriver.idea.server;

import org.jetbrains.annotations.NotNull;

public class JstdServerSettings {

  private final int myPort;
  private final int myBrowserTimeoutMillis;
  private final RunnerMode myRunnerMode;

  public JstdServerSettings(int port, int browserTimeoutMillis, @NotNull RunnerMode runnerMode) {
    myPort = port;
    myBrowserTimeoutMillis = browserTimeoutMillis;
    myRunnerMode = runnerMode;
  }

  public int getPort() {
    return myPort;
  }

  public int getBrowserTimeoutMillis() {
    return myBrowserTimeoutMillis;
  }

  @NotNull
  public RunnerMode getRunnerMode() {
    return myRunnerMode;
  }

  public enum RunnerMode {
    DEBUG, DEBUG_NO_TRACE, DEBUG_OBSERVE, PROFILE, QUIET, INFO
  }

  @Override
  public String toString() {
    return "port=" + myPort +
           ", browserTimeout=" + myBrowserTimeoutMillis +
           ", runnerMode=" + myRunnerMode;
  }

  public static class Builder {
    private int myPort = 9876;
    private int myBrowserTimeoutMillis = 30000;
    private RunnerMode myRunnerMode = RunnerMode.QUIET;

    @NotNull
    public Builder setPort(int port) {
      myPort = port;
      return this;
    }

    @NotNull
    public Builder setBrowserTimeoutMillis(int browserTimeoutMillis) {
      myBrowserTimeoutMillis = browserTimeoutMillis;
      return this;
    }

    @NotNull
    public Builder setRunnerMode(@NotNull RunnerMode runnerMode) {
      myRunnerMode = runnerMode;
      return this;
    }

    public JstdServerSettings build() {
      return new JstdServerSettings(myPort, myBrowserTimeoutMillis, myRunnerMode);
    }
  }

}
