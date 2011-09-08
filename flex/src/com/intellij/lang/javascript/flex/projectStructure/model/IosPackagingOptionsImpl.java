package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.util.xmlb.annotations.Tag;

public class IosPackagingOptionsImpl implements ModifiableIosPackagingOptions {

  public IosPackagingOptionsImpl getCopy() {
    return new IosPackagingOptionsImpl();
  }

  public State getState() {
    return new State();
  }

  public void loadState(State state) {
  }

  @Tag("packaging-ios")
  public static class State {
  }
}
