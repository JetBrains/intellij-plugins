package com.intellij.lang.javascript.flex.projectStructure.model;

public enum TargetPlatform {
  Web("Web"),
  Desktop("Desktop"),
  Mobile("Mobile");

  private final String myPresentableText;

  public String getPresentableText() {
    return myPresentableText;
  }

  TargetPlatform(final String presentableText) {
    myPresentableText = presentableText;
  }
}
