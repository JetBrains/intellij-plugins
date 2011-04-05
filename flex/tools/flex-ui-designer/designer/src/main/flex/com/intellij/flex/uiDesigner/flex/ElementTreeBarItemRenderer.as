package com.intellij.flex.uiDesigner.flex {
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.TextFormatID;
import cocoa.plaf.basic.LabeledItemRenderer;

public class ElementTreeBarItemRenderer extends LabeledItemRenderer {
  override public function set laf(value:LookAndFeel):void {
    super.laf = value;
    labelHelper.textFormat = _laf.getTextFormat(TextFormatID.SMALL_SYSTEM);
  }

  override protected function measure():void {
    if (labelHelper.hasText) {
      labelHelper.validate();
      measuredMinWidth = measuredWidth = Math.round(labelHelper.textWidth) + 5;
      measuredHeight = Math.round(labelHelper.textHeight);
    }
    else {
      measuredMinWidth = measuredWidth = 0;
      measuredHeight =  0;
    }
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    labelHelper.validate();
    labelHelper.y = h;
  }
}
}