package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableIosPackagingOptions;
import com.intellij.util.xmlb.annotations.Tag;

class IosPackagingOptionsImpl implements ModifiableIosPackagingOptions {

  public IosPackagingOptionsImpl getCopy() {
    return new IosPackagingOptionsImpl();
  }

  public State getState() {
    return new State();
  }

  public void loadState(State state) {
  }

  void applyTo(IosPackagingOptionsImpl copy) {

  }

  public boolean isEqual(IosPackagingOptionsImpl iosPackagingOptions) {
    return true;
  }

  @Tag("packaging-ios")
  public static class State {
  }
}
