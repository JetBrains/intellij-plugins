package com.intellij.flex.uiDesigner.mxml;

import org.jetbrains.annotations.Nullable;

class Scope {
  private final DynamicObjectContext owner;
  final boolean staticObjectPointToRootScope;

  public int referenceCounter;

  public Scope(boolean staticObjectPointToRootScope) {
    this.owner = null;
    this.staticObjectPointToRootScope = staticObjectPointToRootScope;
  }

  public Scope(@Nullable DynamicObjectContext owner) {
    this.owner = owner;
    staticObjectPointToRootScope = false;
  }

  public Scope() {
    this(null);
  }

  public DynamicObjectContext getOwner() {
    return owner;
  }
}
