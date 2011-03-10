package com.intellij.flex.uiDesigner.mxml;

abstract class Context {
  protected StaticObjectContext backSibling;
  protected Scope parentScope;
  
  protected DeferredInstanceFromObjectReference deferredInstanceFromObjectReference;
  
  protected int id = -1;
  
  // state index => AddItems
  AddItems[] activeAddItems;
  
  abstract Scope getParentScope();
  
  abstract void referenceInitialized();
  abstract Scope getScope();
  
  final boolean ownerIsDynamic() {
    return getScope().getOwner() != null;
  }
  
  int getId() {
    return id;
  }

  void setId(int value) {
    id = value;
  }

  public DeferredInstanceFromObjectReference getDeferredInstanceFromObjectReference() {
    return deferredInstanceFromObjectReference;
  }

  public void setDeferredInstanceFromObjectReference(DeferredInstanceFromObjectReference value) {
    assert deferredInstanceFromObjectReference == null || value == null;
    deferredInstanceFromObjectReference = value;
  }

  public StaticObjectContext getBackSibling() {
    return backSibling;
  }

  public void setBackSibling(StaticObjectContext backSibling) {
    this.backSibling = backSibling;
  }
}