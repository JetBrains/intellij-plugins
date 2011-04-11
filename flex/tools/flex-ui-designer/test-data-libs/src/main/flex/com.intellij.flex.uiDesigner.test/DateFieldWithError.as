package com.intellij.flex.uiDesigner.test {
import mx.controls.DateField;

public class DateFieldWithError extends DateField {
  override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
    throw new Error("I am runtime error");
    //noinspection UnreachableCodeJS
    super.updateDisplayList(unscaledWidth, unscaledHeight);
  }
}
}
