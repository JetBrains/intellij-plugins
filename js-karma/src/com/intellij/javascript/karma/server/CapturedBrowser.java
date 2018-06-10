package com.intellij.javascript.karma.server;

import org.jetbrains.annotations.NotNull;

public class CapturedBrowser {

  private final String myName;
  private final String myConnectionId;
  private final boolean myAutoCaptured;

  public CapturedBrowser(@NotNull String name, @NotNull String connectionId, boolean autoCaptured) {
    myName = name;
    myConnectionId = connectionId;
    myAutoCaptured = autoCaptured;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public String getConnectionId() {
    return myConnectionId;
  }

  public boolean isAutoCaptured() {
    return myAutoCaptured;
  }

}
