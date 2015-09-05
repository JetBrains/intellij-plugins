package com.intellij.flex.uiDesigner.mxml;

import org.jetbrains.annotations.NotNull;

final class InnerComponentContext extends Context {
  private Scope scope;

  InnerComponentContext(@NotNull Scope parentScope, MxmlObjectReference mxmlObjectReference, int id) {
    super(parentScope);
    assert id != -1;
    this.id = id;
    this.mxmlObjectReference = mxmlObjectReference;
    if (this.mxmlObjectReference != null) {
      assert this.mxmlObjectReference.id == id;
    }
  }

  @Override
  Scope getScope() {
    if (scope == null) {
      scope = new Scope(true);
    }

    return scope;
  }
}