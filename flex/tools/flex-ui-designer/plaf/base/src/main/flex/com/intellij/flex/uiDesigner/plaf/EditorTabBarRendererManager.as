package com.intellij.flex.uiDesigner.plaf {
import cocoa.Insets;
import cocoa.ItemMouseSelectionMode;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.TextFormatId;
import cocoa.renderer.InteractiveGraphicsRendererManager;

import flash.display.GradientType;
import flash.display.Graphics;
import flash.display.GraphicsGradientFill;
import flash.display.GraphicsSolidFill;
import flash.display.GraphicsStroke;
import flash.display.IGraphicsData;
import flash.geom.Matrix;
import flash.text.engine.TextLine;

public class EditorTabBarRendererManager extends InteractiveGraphicsRendererManager {
  private static const sharedMatrix:Matrix = new Matrix();

  private static const RIGHT_BLOCK_COLOR:uint = 0xc0c0c0;

  private static const ARC_SIZE:Number = 4;
  private static const HEIGHT:Number = 20;

  private static const inactiveStroke:GraphicsStroke = new GraphicsStroke(1);
  inactiveStroke.fill = new GraphicsSolidFill(0x808080);
  private static const inactiveFill:GraphicsGradientFill = new GraphicsGradientFill(GradientType.LINEAR, [0xffffff, 0xeeeeee], [1, 1],
                                                                                    [0, 255], sharedMatrix);

  private static const graphicsData:Vector.<IGraphicsData> = new <IGraphicsData>[inactiveStroke, inactiveFill];
  private static const graphicsData2:Vector.<IGraphicsData> = new <IGraphicsData>[inactiveStroke];

  public function EditorTabBarRendererManager(laf:LookAndFeel, lafKey:String) {
    super(laf.getTextFormat(TextFormatId.SYSTEM), new Insets(7, 0, 0, 5));
  }


  override public function get mouseSelectionMode():int {
    return ItemMouseSelectionMode.DOWN;
  }

  override protected function computeCreatingRendererSize(w:Number, h:Number, line:TextLine):void {
    _lastCreatedRendererWidth = Math.round(line.textWidth) + (1 + 6 + 1 + 2 + 16 + 2);
  }

  override public function setSelected(itemIndex:int, value:Boolean):void {
    super.setSelecting(itemIndex, value);
  }

  override protected function drawEntry(itemIndex:int, g:Graphics, w:Number, h:Number, x:Number, y:Number):void {
    if (_selectionModel.isItemSelected(itemIndex)) {
      drawSelected(itemIndex, x, w);
    }
    else {
      drawNotSelected(g, w, itemIndex);
    }
  }

  private function drawSelected(itemIndex:int, x:Number, w:Number):void {
    var g:Graphics = _textLineContainer.graphics;
    g.clear();

    g.lineStyle(1, IdeaLookAndFeel.BORDER_COLOR);
    g.beginFill(0xffffff);

    const bottomY:Number = HEIGHT + 0.5;
    const leftX:Number = x + 0.5;
    g.moveTo(0.5, 24.5);
    if (x != 0) {
      g.lineTo(0.5, bottomY);
      g.lineTo(leftX, bottomY);
    }

    drawRect(g, leftX, -1.5, (x + w) - 0.5, bottomY, isLast(itemIndex));
    const tabViewRightX:Number = EditorTabViewSkin(_container.parent).width - 0.5;
    g.lineTo(tabViewRightX, bottomY);
    g.lineTo(tabViewRightX, 24.5);
    g.lineTo(0.5, 24.5);
    g.endFill();

    const tabViewBottomY:Number = EditorTabViewSkin(_container.parent).height - 0.5;
    g.lineTo(0.5, tabViewBottomY);
    g.lineTo(tabViewRightX, tabViewBottomY);
    g.lineTo(tabViewRightX, 24.5);
  }

  private function drawNotSelected(g:Graphics, w:Number, itemIndex:int):void {
    sharedMatrix.createGradientBox(w - 1, HEIGHT - 1, Math.PI / 2, 0.5, 0.5);
    g.drawGraphicsData(graphicsData);

    var rightX:Number = w - 0.5;
    if (!isLast(itemIndex) && /* leftFromSelection*/ itemIndex == (_selectionModel.selectedIndex - 1)) {
      rightX += ARC_SIZE + 1;
    }

    const topY:Number = 0.5;
    var leftX:Number;
    const bottomY:Number = HEIGHT + 0.5;
    if (itemIndex == 0) {
      leftX = 0.5;
      g.moveTo(leftX, bottomY);
      drawRect(g, leftX, topY, rightX, bottomY, true);
    }
    else {
      leftX = 0.5;
      g.moveTo(leftX, bottomY);
      g.lineStyle();
      g.lineTo(leftX, topY);
      g.drawGraphicsData(graphicsData2);
      g.lineTo(rightX - ARC_SIZE, topY);
      g.curveTo(rightX, topY, rightX, topY + ARC_SIZE);
      g.lineTo(rightX, bottomY);
    }
    g.lineStyle();
    g.lineTo(leftX, bottomY);
    g.endFill();

    if (itemIndex != 0) {
      g.drawGraphicsData(graphicsData2);
      g.moveTo(- (ARC_SIZE + 1), topY);
      g.lineTo(0.5, topY);
    }

    g.lineStyle(1, RIGHT_BLOCK_COLOR);
    g.moveTo(rightX - 1, ARC_SIZE - 1);
    g.lineTo(rightX - 1, HEIGHT);
  }

  private static function drawRect(g:Graphics, leftX:Number, topY:Number, rightX:Number, bottomY:Number, isLast:Boolean):void {
    g.lineTo(leftX, topY + ARC_SIZE);
    g.curveTo(leftX, topY, leftX + ARC_SIZE, topY);

    g.lineTo(rightX - ARC_SIZE, topY);
    g.curveTo(rightX, topY, rightX, topY + ARC_SIZE);

    if (isLast) {
      g.lineTo(rightX, bottomY);
    }
    else {
      g.lineTo(rightX, bottomY - ARC_SIZE);
      g.curveTo(rightX, bottomY, rightX + ARC_SIZE, bottomY);
    }
  }
}
}
