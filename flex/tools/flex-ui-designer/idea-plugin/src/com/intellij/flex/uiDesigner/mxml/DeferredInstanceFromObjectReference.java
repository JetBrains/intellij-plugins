package com.intellij.flex.uiDesigner.mxml;

class DeferredInstanceFromObjectReference {
  private int objectInstance;
  private final int deferredParentInstance;

  public DeferredInstanceFromObjectReference(int objectInstance, int deferredParentInstance) {
    this.objectInstance = objectInstance;
    this.deferredParentInstance = deferredParentInstance;
  }

  public boolean isWritten() {
    return objectInstance == -1;
  }

  public void markAsWritten() {
    objectInstance = -1;
  }

  public int getObjectInstance() {
    return objectInstance;
  }

  public int getDeferredParentInstance() {
    return deferredParentInstance;
  }
}