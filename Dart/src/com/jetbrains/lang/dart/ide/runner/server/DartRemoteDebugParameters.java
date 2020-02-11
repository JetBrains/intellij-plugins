// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server;

import org.jetbrains.annotations.NotNull;

public class DartRemoteDebugParameters implements Cloneable {
  @NotNull private String myDartProjectPath = "";

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
