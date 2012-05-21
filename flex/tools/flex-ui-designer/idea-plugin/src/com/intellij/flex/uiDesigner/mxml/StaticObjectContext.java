package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

class StaticObjectContext extends Context {
  private int referencePosition;
  private final PrimitiveAmfOutputStream out;

  public StaticObjectContext(int referencePosition, PrimitiveAmfOutputStream out, int suggestedId, Scope parentScope) {
    this(referencePosition, out, parentScope);
    if (suggestedId != -1) {
      id = suggestedId;
      referenceInitialized();
    }
  }

  public StaticObjectContext(int referencePosition, PrimitiveAmfOutputStream out, Scope parentScope) {
    super(parentScope);
    this.referencePosition = referencePosition;
    this.out = out;
  }

  @Override
  protected void referenceInitialized() {
    assert referencePosition != -1;
    initializeReference(id, out, referencePosition);
  }

  @Override
  Scope getScope() {
    return parentScope;
  }

  static void initializeReference(int id, PrimitiveAmfOutputStream out, int referencePosition) {
    out.putShort(id + 1, referencePosition);
  }

  public StaticObjectContext reinitialize(int referencePosition, int id) {
    childrenType = null;
    cssRulesetDefined = false;
    mxmlObjectReference = null;

    this.referencePosition = referencePosition;
    staticInstanceReferenceInDeferredParentInstance = null;
    this.id = id;
    if (id != -1) {
      referenceInitialized();
    }

    return this;
  }
}
