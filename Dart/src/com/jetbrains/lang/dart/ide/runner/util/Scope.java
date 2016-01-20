package com.jetbrains.lang.dart.ide.runner.util;

public enum Scope {
  FOLDER("All in folder"), FILE("All in file"), GROUP("Test group"), METHOD("Single test");

  private final String myPresentableName;

  Scope(final String name) {
    myPresentableName = name;
  }

  public String getPresentableName() {
    return myPresentableName;
  }

  public boolean expectsTestName() {
    return this == GROUP || this == METHOD;
  }
}
