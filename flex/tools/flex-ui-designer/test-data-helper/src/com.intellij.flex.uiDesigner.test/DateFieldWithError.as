package com.intellij.flex.uiDesigner.test {
import mx.controls.DateField;

public class DateFieldWithError extends DateField {
  override protected function commitProperties():void {
    super.commitProperties();

    throw new Error("I am runtime error");
  }
}
}