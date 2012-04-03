package com.intellij.flex.uiDesigner.flex {
import mx.core.UIComponent;
import mx.core.mx_internal;

use namespace mx_internal;

public class UnknownComponent extends UIComponent {
  private const unknownComponentHelper:UnknownComponentHelper = new UnknownComponentHelper();
  private var statusText:String;

  // todo unknown component class name
  public function UnknownComponent(statusText:String = "Unknown component") {
    this.statusText = statusText;
  }

  override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
    unknownComponentHelper.draw(unscaledWidth, unscaledHeight, this, createStatusText());
  }

  protected function createStatusText():String {
    return statusText;
  }

  override public function set scaleX(value:Number):void {
    $scaleX = value;
  }

  override public function set scaleY(value:Number):void {
    $scaleY = value;
  }
}
}
