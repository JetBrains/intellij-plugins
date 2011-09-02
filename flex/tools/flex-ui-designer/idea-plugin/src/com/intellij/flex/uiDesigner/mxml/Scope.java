package com.intellij.flex.uiDesigner.mxml;

class Scope {
  private final DynamicObjectContext owner;

  public int referenceCounter;

  public Scope(DynamicObjectContext owner) {
    this.owner = owner;
  }

  public Scope() {
    owner = null;
  }

  public DynamicObjectContext getOwner() {
    return owner;
  }
}
