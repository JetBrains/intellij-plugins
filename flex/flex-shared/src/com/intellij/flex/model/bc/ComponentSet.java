package com.intellij.flex.model.bc;

public enum ComponentSet {
  SparkAndMx("Spark + MX"),
  SparkOnly("Spark only"),
  MxOnly("MX only");

  private final String myPresentableText;

  public String getPresentableText() {
    return myPresentableText;
  }

  ComponentSet(final String presentableText) {
    myPresentableText = presentableText;
  }
}
