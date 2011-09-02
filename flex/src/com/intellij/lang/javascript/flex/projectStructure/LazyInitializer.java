package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.openapi.Disposable;

/**
 * User: ksafonov
 */
public abstract class LazyInitializer {

  private boolean myInitialized;

  public void ensureInitialized() {
    if (!myInitialized) {
      initialize();
      myInitialized = true;
    }
  }

  protected abstract void initialize();

  public void dispose() {
    myInitialized = false;
  }
}
