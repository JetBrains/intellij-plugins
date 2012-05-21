package com.intellij.flex.uiDesigner.mxml;

import org.jetbrains.annotations.NotNull;

final class NullContext extends Context {
  private Scope tempParentScope;

  NullContext(Scope rootScope) {
    super(rootScope);
  }

  void setTempParentScope(Scope value) {
    tempParentScope = value;
  }

  @NotNull
  @Override
  Scope getParentScope() {
    return tempParentScope == null ? parentScope : tempParentScope;
  }

  @Override
  Scope getScope() {
    throw new UnsupportedOperationException();
  }

  public void reset() {
    cssRulesetDefined = false;
    id = -1;
    mxmlObjectReference = null;
    tempParentScope = null;
  }
}