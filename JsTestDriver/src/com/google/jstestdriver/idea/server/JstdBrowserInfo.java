package com.google.jstestdriver.idea.server;

import org.jetbrains.annotations.NotNull;

public class JstdBrowserInfo {
  private final String myId;
  private final String myName;

  public JstdBrowserInfo(@NotNull String id, @NotNull String name) {
    myId = id;
    myName = name;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Override
  public String toString() {
    return "id=" + myId + ", name=" + myName;
  }
}
