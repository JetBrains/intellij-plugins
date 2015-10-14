package com.intellij.flex.uiDesigner.mxml;

import org.jetbrains.annotations.Nullable;

class Scope {
  private final DynamicObjectContext owner;
  final boolean staticObjectPointToScope;

  public int referenceCounter;

  public Scope(boolean staticObjectPointToScope) {
    this.owner = null;
    this.staticObjectPointToScope = staticObjectPointToScope;
  }

  public Scope(@Nullable DynamicObjectContext owner) {
    this.owner = owner;
    staticObjectPointToScope = false;
  }

  public Scope() {
    this(null);
  }

  public DynamicObjectContext getOwner() {
    return owner;
  }
}
