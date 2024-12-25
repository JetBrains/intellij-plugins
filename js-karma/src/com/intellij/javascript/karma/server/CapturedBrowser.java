// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

  public @NotNull String getName() {
    return myName;
  }

  public @NotNull String getConnectionId() {
    return myConnectionId;
  }

  public boolean isAutoCaptured() {
    return myAutoCaptured;
  }

}
