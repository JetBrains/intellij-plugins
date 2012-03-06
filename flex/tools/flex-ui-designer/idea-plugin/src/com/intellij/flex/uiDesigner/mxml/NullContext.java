package com.intellij.flex.uiDesigner.mxml;

class NullContext extends Context {
  NullContext(Scope rootScope) {
    parentScope = rootScope;
  }

  @Override
  void referenceInitialized() {
  }

  @Override
  Scope getScope() {
    throw new UnsupportedOperationException();
  }

  public void reset() {
    cssRulesetDefined = false;
    id = -1;
    mxmlObjectReference = null;
  }
}
