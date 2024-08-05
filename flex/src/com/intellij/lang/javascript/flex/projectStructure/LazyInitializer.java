// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure;

import org.jetbrains.annotations.Nullable;

public abstract class LazyInitializer<T> {

  private @Nullable T myInitializedFor;

  public void ensureInitialized(T t) {
    if (myInitializedFor == null) {
      initialize(t);
      myInitializedFor = t;
    }
    else if (myInitializedFor != t) {
      throw new IllegalArgumentException(
        "Trying to initialize for different entity " + t + ", was originally initialized for " + myInitializedFor);
    }
  }

  protected abstract void initialize(T t);

  public final void dispose() {
    myInitializedFor = null;
    doDispose();
  }

  protected abstract void doDispose();
}
