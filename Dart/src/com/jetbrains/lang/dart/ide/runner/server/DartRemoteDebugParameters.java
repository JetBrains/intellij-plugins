package com.jetbrains.lang.dart.ide.runner.server;

import org.jetbrains.annotations.NotNull;

public class DartRemoteDebugParameters implements Cloneable {
  private static final String DEFAULT_DART_DEBUG_HOST = "localhost";
  private static final int DEFAULT_DART_DEBUG_PORT = 5858;

  @NotNull private String myHost = DEFAULT_DART_DEBUG_HOST;
  private int myPort = DEFAULT_DART_DEBUG_PORT;
  @NotNull private String myDartProjectPath = "";

  @NotNull
  public String getHost() {
    return myHost;
  }

  public void setHost(@NotNull final String host) {
    myHost = host.isEmpty() ? DEFAULT_DART_DEBUG_HOST : host;
  }

  public int getPort() {
    return myPort;
  }

  public void setPort(final int port) {
    myPort = port;
  }

  @NotNull
  public String getDartProjectPath() {
    return myDartProjectPath;
  }

  public void setDartProjectPath(@NotNull final String dartProjectPath) {
    myDartProjectPath = dartProjectPath;
  }

  @Override
  protected DartRemoteDebugParameters clone() {
    try {
      return (DartRemoteDebugParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
