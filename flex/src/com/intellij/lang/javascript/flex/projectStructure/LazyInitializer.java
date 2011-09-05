package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.openapi.Disposable;

/**
 * User: ksafonov
 */
public abstract class LazyInitializer<T> {

  private boolean myInitialized;

  public void ensureInitialized(T t) {
    if (!myInitialized) {
      initialize(t);
      myInitialized = true;
    }
  }

  protected abstract void initialize(T t);

  public void dispose() {
    myInitialized = false;
  }
}
