package com.intellij.lang.javascript.flex.projectStructure;

import org.jetbrains.annotations.Nullable;

/**
 * User: ksafonov
 */
public abstract class LazyInitializer<T> {

  @Nullable
  private T myInitializedFor;

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
