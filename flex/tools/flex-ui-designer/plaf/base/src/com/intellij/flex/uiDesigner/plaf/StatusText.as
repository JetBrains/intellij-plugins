package com.intellij.flex.uiDesigner.plaf {
import cocoa.Border;
import cocoa.Insets;
import cocoa.LabelHelper;
import cocoa.View;
import cocoa.text.TextFormat;

import flash.display.Graphics;

public class StatusText {
  private var owner:View;
  private var border:Border;
  private var labelHelper:LabelHelper;

  public function StatusText(owner:View, border:Border, textFormat:TextFormat) {
    this.owner = owner;
    this.border = border;
    labelHelper = new LabelHelper(owner, textFormat);
  }

  public function show(g:Graphics, text:String, contentInsets:Insets, w:Number, h:Number):void {
    labelHelper.text = text;
    labelHelper.validate();
    labelHelper.textLine.visible = true;
    const roundedTextWidth:Number = Math.round(labelHelper.textWidth);
    const borderWidth:Number = border.contentInsets.width + roundedTextWidth;
    const borderX:Number = contentInsets.left + ((w - contentInsets.width - borderWidth) * 0.5);
    const borderY:Number = contentInsets.top + Math.round(((h - contentInsets.height - border.layoutHeight) * 0.5));
    labelHelper.move(borderX + border.contentInsets.left, borderY + border.layoutHeight - border.contentInsets.bottom);

    g.beginFill(0xe8e8e8);
    g.drawRect(contentInsets.left, contentInsets.top, w - contentInsets.width, h - contentInsets.height);
    g.endFill();

    border.draw(g, border.contentInsets.width + roundedTextWidth, border.layoutHeight, borderX, borderY);
  }

  public function hide():void {
    if (labelHelper != null && labelHelper.textLine != null) {
      labelHelper.textLine.visible = false;
    }
  }
}
}