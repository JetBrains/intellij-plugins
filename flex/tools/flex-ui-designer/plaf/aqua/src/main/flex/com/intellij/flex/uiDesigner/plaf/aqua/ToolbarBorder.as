package com.intellij.flex.uiDesigner.plaf.aqua {
import cocoa.View;
import cocoa.border.LinearGradientBorder;

import flash.display.Graphics;

public class ToolbarBorder extends LinearGradientBorder {
  public function ToolbarBorder() {
    super([0xc5c5c5, 0xa7a7a7], NaN, NaN);
  }

  override public function draw(view:View, g:Graphics, w:Number, h:Number):void {
    sharedMatrix.createGradientBox(w, h - 2, Math.PI / 2, 0, 0);
    g.drawGraphicsData(graphicsData);
    g.drawRect(0, 0, w, h - 2);
    g.endFill();

    g.lineStyle(1, 0xb2b2b2);
    g.moveTo(0, h - 2);
    g.lineTo(w, h - 2);

    g.lineStyle(1, 0x696969);
    g.moveTo(0, h - 1);
    g.lineTo(w, h - 1);
  }
}
}
