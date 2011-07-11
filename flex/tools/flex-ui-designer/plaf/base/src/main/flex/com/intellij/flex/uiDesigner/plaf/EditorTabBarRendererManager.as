package com.intellij.flex.uiDesigner.plaf {
import cocoa.Insets;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.TextFormatId;
import cocoa.renderer.InteractiveGraphicsRendererManager;

import flash.display.Shape;

public class EditorTabBarRendererManager extends InteractiveGraphicsRendererManager {
  public function EditorTabBarRendererManager(laf:LookAndFeel, lafKey:String) {
    super(laf.getTextFormat(TextFormatId.SYSTEM), new Insets());
  }

  override protected function drawEntry(itemIndex:int, shape:Shape, w:Number, h:Number):void {
    super.drawEntry(itemIndex, shape, w, h);
  }

  //private function drawNotSelected(g:Graphics, w:Number, h:Number):void {
  //  labelHelper.container = this;
  //  labelHelper.validate();
  //  labelHelper.move(1 + 6, h - 5);
  //
  //  sharedMatrix.createGradientBox(w - 1, HEIGHT - 1, Math.PI / 2, 0.5, 0.5);
  //  g.drawGraphicsData(graphicsData);
  //
  //  var rightX:Number = w - 0.5;
  //  if (!isLast && /* leftFromSelection*/ itemIndex == (SingleSelectionDataGroup(parent).selectedIndex - 1)) {
  //    rightX += ARC_SIZE + 1;
  //  }
  //
  //  const topY:Number = 0.5;
  //  var leftX:Number;
  //  const bottomY:Number = HEIGHT + 0.5;
  //  if (itemIndex == 0) {
  //    leftX = 0.5;
  //    g.moveTo(leftX, bottomY);
  //    drawRect(g, leftX, topY, rightX, bottomY, true);
  //  }
  //  else {
  //    leftX = 0.5;
  //    g.moveTo(leftX, bottomY);
  //    g.lineStyle();
  //    g.lineTo(leftX, topY);
  //    g.drawGraphicsData(graphicsData2);
  //    g.lineTo(rightX - ARC_SIZE, topY);
  //    g.curveTo(rightX, topY, rightX, topY + ARC_SIZE);
  //    g.lineTo(rightX, bottomY);
  //  }
  //  g.lineStyle();
  //  g.lineTo(leftX, bottomY);
  //  g.endFill();
  //
  //  if (itemIndex != 0) {
  //    g.drawGraphicsData(graphicsData2);
  //    g.moveTo(- (ARC_SIZE + 1), topY);
  //    g.lineTo(0.5, topY);
  //  }
  //
  //  g.lineStyle(1, RIGHT_BLOCK_COLOR);
  //  g.moveTo(rightX - 1, ARC_SIZE - 1);
  //  g.lineTo(rightX - 1, HEIGHT);
  //}

}
}
