package com.jetbrains.lang.dart.ide.runner.util;

public enum Scope {
  FILE("All in file"), FOLDER("All in folder"), GROUP("Test group"), METHOD("Single test");

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
