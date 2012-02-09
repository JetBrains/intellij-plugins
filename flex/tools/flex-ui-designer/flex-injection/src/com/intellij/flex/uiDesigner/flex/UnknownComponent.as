package com.intellij.flex.uiDesigner.flex {
import mx.core.UIComponent;

public class UnknownComponent extends UIComponent {
  private const unknownComponentHelper:UnknownComponentHelper = new UnknownComponentHelper();

  override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
    unknownComponentHelper.draw(unscaledWidth, unscaledHeight, this, createStatusText());
  }

  protected function createStatusText():String {
    // todo unknown component class name
    return "Unknown component";
  }
}
}
