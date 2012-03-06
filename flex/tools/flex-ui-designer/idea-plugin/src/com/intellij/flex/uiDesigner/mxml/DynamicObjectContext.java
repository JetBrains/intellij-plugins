package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.ByteRange;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

class DynamicObjectContext extends Context {
  private ByteRange dataRange;
  private Scope scope;

  private boolean written;
  private boolean immediateCreation;

  int overrideUserCount;

  // IDEA-73040
  final ArrayList<State> includeInStates = new ArrayList<State>();

  DynamicObjectContext(NullContext nullContext) {
    this.parentScope = nullContext.getParentScope();
    this.id = nullContext.id;
    this.cssRulesetDefined = nullContext.cssRulesetDefined;
    this.mxmlObjectReference = nullContext.mxmlObjectReference;
  }

  @Override
  void referenceInitialized() {
  }

  @Override
  Scope getScope() {
    if (scope == null) {
      scope = new Scope(this);
    }

    return scope;
  }

  int getReferredObjectsCount() {
    return scope == null ? 0 : scope.referenceCounter;
  }

  public boolean isWritten() {
    return written;
  }

  public void markAsWritten() {
    written = true;
  }

  public boolean isImmediateCreation() {
    return immediateCreation;
  }

  public void setImmediateCreation(boolean immediateCreation) {
    this.immediateCreation = immediateCreation;
  }

  public ByteRange getDataRange() {
    return dataRange;
  }

  public void setDataRange(ByteRange dataRange) {
    this.dataRange = dataRange;
  }
}
