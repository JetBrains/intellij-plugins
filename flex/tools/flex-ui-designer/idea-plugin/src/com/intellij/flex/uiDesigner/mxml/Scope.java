package com.intellij.flex.uiDesigner.mxml;

import org.jetbrains.annotations.Nullable;

class Scope {
  private final DynamicObjectContext owner;
  final boolean staticObjectPointToScope;

  public int referenceCounter;

  Scope(boolean staticObjectPointToScope) {
    this.owner = null;
    this.staticObjectPointToScope = staticObjectPointToScope;
  }

  Scope(@Nullable DynamicObjectContext owner) {
    this.owner = owner;
    staticObjectPointToScope = false;
  }

  Scope() {
    this(null);
  }

  public DynamicObjectContext getOwner() {
    return owner;
  }
}
