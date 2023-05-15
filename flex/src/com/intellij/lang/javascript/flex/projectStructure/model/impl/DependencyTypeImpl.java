package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.flex.model.bc.LinkageType;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyType;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableDependencyType;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

class DependencyTypeImpl implements ModifiableDependencyType {

  @NotNull
  private LinkageType myLinkageType = DEFAULT_LINKAGE;

  @Override
  @NotNull
  public LinkageType getLinkageType() {
    return myLinkageType;
  }

  @Override
  public void setLinkageType(@NotNull LinkageType linkageType) {
    myLinkageType = linkageType;
  }

  public State getState() {
    State state = new State();
    state.LINKAGE_TYPE = myLinkageType.getSerializedText();
    return state;
  }

  public void loadState(State state) {
    myLinkageType = LinkageType.valueOf(state.LINKAGE_TYPE, DEFAULT_LINKAGE);
  }

  public void applyTo(ModifiableDependencyType copy) {
    copy.setLinkageType(myLinkageType);
  }

  @Override
  public void copyFrom(DependencyType dependencyType) {
    myLinkageType = dependencyType.getLinkageType();
  }

  @Override
  public boolean isEqual(DependencyType other) {
    return myLinkageType == other.getLinkageType();
  }

  @Tag("dependency")
  public static class State {

    @Attribute("linkage")
    public String LINKAGE_TYPE;
  }
}
