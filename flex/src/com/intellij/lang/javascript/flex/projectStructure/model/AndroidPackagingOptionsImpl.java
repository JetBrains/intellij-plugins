package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.util.xmlb.annotations.Tag;

public class AndroidPackagingOptionsImpl implements ModifiableAndroidPackagingOptions {

  public AndroidPackagingOptionsImpl getCopy() {
    return new AndroidPackagingOptionsImpl();
  }
  
  public State getState() {
    return new State();
  }
  
  public void loadState(State state) {
  }
  
  @Tag("packaging-android")
  public static class State {
  }
  
}
