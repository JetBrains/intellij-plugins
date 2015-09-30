package com.jetbrains.lang.dart.ide.runner.util;

public enum Scope {
  ALL("All in file"), GROUP("Test group"), METHOD("Single test");

  private final String myPresentableName;

  Scope(final String name) {
    myPresentableName = name;
  }

  public String getPresentableName() {
    return myPresentableName;
  }
}
