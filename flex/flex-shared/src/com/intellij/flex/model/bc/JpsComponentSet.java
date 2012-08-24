package com.intellij.flex.model.bc;

public enum JpsComponentSet {
  SparkAndMx("Spark + MX"),
  SparkOnly("Spark only"),
  MxOnly("MX only");

  private final String myPresentableText;

  public String getPresentableText() {
    return myPresentableText;
  }

  JpsComponentSet(final String presentableText) {
    myPresentableText = presentableText;
  }
}
