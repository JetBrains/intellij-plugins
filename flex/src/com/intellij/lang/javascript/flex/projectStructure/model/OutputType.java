package com.intellij.lang.javascript.flex.projectStructure.model;

public enum OutputType {
  Application("Application (*.swf)"),
  RuntimeLoadedModule("Runtime loaded module (*.swf)"),
  Library("Library (*.swc)");

  private final String myPresentableText;

  public String getPresentableText() {
    return myPresentableText;
  }

  OutputType(final String presentableText) {
    myPresentableText = presentableText;
  }
}
