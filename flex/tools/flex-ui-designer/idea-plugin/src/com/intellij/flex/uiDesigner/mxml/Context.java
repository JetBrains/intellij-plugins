package com.intellij.flex.uiDesigner.mxml;

import org.jetbrains.annotations.Nullable;

abstract class Context {
  protected StaticObjectContext backSibling;
  protected Scope parentScope;
  protected boolean cssRulesetDefined;

  protected StaticInstanceReferenceInDeferredParentInstance staticInstanceReferenceInDeferredParentInstance;

  protected int id = -1;

  // state index => AddItems
  AddItems[] activeAddItems;

  protected String childrenType;

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

  public StaticInstanceReferenceInDeferredParentInstance getStaticInstanceReferenceInDeferredParentInstance() {
    return staticInstanceReferenceInDeferredParentInstance;
  }

  public void setStaticInstanceReferenceInDeferredParentInstance(
      @Nullable StaticInstanceReferenceInDeferredParentInstance value) {
    assert staticInstanceReferenceInDeferredParentInstance == null || value == null;
    staticInstanceReferenceInDeferredParentInstance = value;
  }

  public StaticObjectContext getBackSibling() {
    return backSibling;
  }

  public void setBackSibling(StaticObjectContext backSibling) {
    this.backSibling = backSibling;
  }

  @Nullable
  public String getChildrenType() {
    return childrenType;
  }

  public void setChildrenType(String value) {
    assert childrenType == null;
    childrenType = value;
  }

  public boolean isCssRulesetDefined() {
    return cssRulesetDefined;
  }
  
  public void markCssRulesetDefined() {
    assert !cssRulesetDefined;
    cssRulesetDefined = true;
  }
}