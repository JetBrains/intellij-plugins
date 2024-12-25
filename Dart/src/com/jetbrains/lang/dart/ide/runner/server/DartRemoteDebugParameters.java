// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server;

import org.jetbrains.annotations.NotNull;

public class DartRemoteDebugParameters implements Cloneable {
  private @NotNull String myDartProjectPath = "";

  public @NotNull String getDartProjectPath() {
    return myDartProjectPath;
  }

  public void setDartProjectPath(final @NotNull String dartProjectPath) {
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
