package com.intellij.flex.uiDesigner.test {
import spark.components.Label;

public class LabelWithError extends Label {
  public function LabelWithError() {
    throw new Error("Boo");
  }
}
}