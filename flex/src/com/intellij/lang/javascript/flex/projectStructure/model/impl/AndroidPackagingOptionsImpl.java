package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAndroidPackagingOptions;
import com.intellij.util.xmlb.annotations.Tag;

class AndroidPackagingOptionsImpl implements ModifiableAndroidPackagingOptions {

  public AndroidPackagingOptionsImpl getCopy() {
    return new AndroidPackagingOptionsImpl();
  }

  public State getState() {
    return new State();
  }

  public void loadState(State state) {
  }

  void applyTo(AndroidPackagingOptionsImpl copy) {
  }

  @Tag("packaging-android")
  public static class State {
  }

}
