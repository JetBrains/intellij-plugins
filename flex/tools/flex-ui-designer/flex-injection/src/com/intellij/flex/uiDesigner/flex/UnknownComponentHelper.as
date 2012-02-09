package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.util.CustomComponentPainter;

import flash.display.Graphics;
import flash.text.engine.TextLine;

import mx.core.UIComponent;
import mx.core.mx_internal;

use namespace mx_internal;

public final class UnknownComponentHelper {
  private var statusTextLine:TextLine;
  private var statusText:String;

  public function draw(w:Number, h:Number, uiComponent:UIComponent, newStatusText:String):void {
    var g:Graphics = uiComponent.graphics;
    g.clear();

    if (isNaN(w) || isNaN(h) || w == 0 || h == 0) {
      return;
    }

    if (newStatusText != statusText) {
      statusText = newStatusText;
      statusTextLine = CustomComponentPainter.createTextLine(newStatusText, statusTextLine, w);
      if (statusTextLine != null && statusTextLine.parent == null) {
        uiComponent.$addChild(statusTextLine);
      }
    }

    if (statusTextLine != null) {
      CustomComponentPainter.layoutTextLine(statusTextLine, w, h);
    }

    CustomComponentPainter.paint(g, w, h);
  }
}
}
