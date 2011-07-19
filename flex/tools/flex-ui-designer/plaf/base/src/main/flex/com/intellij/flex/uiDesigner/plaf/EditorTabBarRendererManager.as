package com.intellij.flex.uiDesigner.plaf {
import cocoa.AbstractView;
import cocoa.Component;
import cocoa.Insets;
import cocoa.ItemMouseSelectionMode;
import cocoa.PushButton;
import cocoa.SegmentedControl;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.TextFormatId;
import cocoa.renderer.CompositeEntry;
import cocoa.renderer.CompositeEntryFactory;
import cocoa.renderer.InteractiveGraphicsRendererManager;
import cocoa.renderer.LayeringMode;
import cocoa.renderer.TextLineAndDisplayObjectEntry;
import cocoa.renderer.TextLineAndDisplayObjectEntryFactory;

import flash.display.DisplayObject;
import flash.display.GradientType;
import flash.display.Graphics;
import flash.display.GraphicsGradientFill;
import flash.display.GraphicsSolidFill;
import flash.display.GraphicsStroke;
import flash.display.IGraphicsData;
import flash.display.Shape;
import flash.geom.Matrix;
import flash.text.engine.TextLine;

import mx.core.IUIComponent;

public class EditorTabBarRendererManager extends InteractiveGraphicsRendererManager {
  private static const sharedMatrix:Matrix = new Matrix();

  private static const ARC_SIZE:Number = 4;

  private static const inactiveStroke:GraphicsStroke = new GraphicsStroke(1);
  inactiveStroke.fill = new GraphicsSolidFill(0x808080);
  private static const inactiveFill:GraphicsGradientFill = new GraphicsGradientFill(GradientType.LINEAR, [0xffffff, 0xeeeeee], [1, 1], [0, 255], sharedMatrix);

  private static const inactiveStrokeAndFillData:Vector.<IGraphicsData> = new <IGraphicsData>[inactiveStroke, inactiveFill];
  private static const inactiveStrokeData:Vector.<IGraphicsData> = new <IGraphicsData>[inactiveStroke];

  protected var myFactory:CompositeEntryFactory = new CompositeEntryFactory(1);
  private var laf:LookAndFeel;

  //noinspection JSUnusedLocalSymbols
  public function EditorTabBarRendererManager(laf:LookAndFeel, lafKey:String) {
    super(laf.getTextFormat(TextFormatId.SYSTEM), new Insets(7, 0, 0, 5));
    this.laf = laf;
  }

  override protected function createFactory():TextLineAndDisplayObjectEntryFactory {
    return myFactory;
  }

  override protected function get layeringMode():int {
    return LayeringMode.DESCENDING_ORDER;
  }

  override public function get mouseSelectionMode():int {
    return ItemMouseSelectionMode.DOWN;
  }

  override protected function computeCreatingRendererSize(w:Number, h:Number, line:TextLine):void {
    _lastCreatedRendererDimension = Math.round(line.textWidth) + (1 + 6 + 1 + 2 + 16 + 2);
  }

  override protected function doCreateEntry(line:TextLine, itemIndex:int):TextLineAndDisplayObjectEntry {
    var entry:CompositeEntry = CompositeEntry(myFactory.create(line));
    var closeButton:PushButton = PushButton(entry.components[0]);
    if (closeButton == null) {
      closeButton = new PushButton();
      entry.components[0] = closeButton;
      closeButton.setAction(closeTab, itemIndex);
      closeButton.lafSubkey = "TabLabel";
    }

    return entry;
  }

  override protected function addToDisplayList(entry:TextLineAndDisplayObjectEntry, displayIndex:int):void {
    super.addToDisplayList(entry, displayIndex);

    var needSetSize:Boolean;
    var closeButton:Component = CompositeEntry(entry).components[0];
    var skin:AbstractView = AbstractView(closeButton.skin);
    if (skin == null) {
      if (!_container.mouseChildren) {
        _container.mouseChildren = true;
        _textLineContainer.mouseChildren = true;
      }
      skin = AbstractView(closeButton.createView(laf));
      needSetSize = true;
    }

    _textLineContainer.addChild(DisplayObject(skin));

    if (needSetSize) {
      skin.nestLevel = SegmentedControl(_container).nestLevel + 1;
      skin.initialize();
      skin.validateNow();
      skin.setActualSize(skin.getExplicitOrMeasuredWidth(), skin.getExplicitOrMeasuredHeight());
    }
  }

  private function closeTab(itemIndex:int):void {
    EditorTabViewSkin(_container.parent).closeTab(itemIndex);
  }

  override public function setSelected(itemIndex:int, relatedIndex:int, value:Boolean):void {
    var entry:TextLineAndDisplayObjectEntry = findEntry2(itemIndex);
    var g:Graphics = Shape(entry.displayObject).graphics;
    g.clear();
    if (value) {
      const oldRightNeighbour:int = relatedIndex - 1;
      if (oldRightNeighbour > -1 && oldRightNeighbour != itemIndex) {
        // redraw prev item, because see rightNeighbourIsSelected
        setSelected(oldRightNeighbour, -1, false);
      }
      drawSelected(itemIndex, entry.displayObject.x, entry.line.userData);
    }
    else {
      if (relatedIndex != -1 && itemIndex != 0 && (itemIndex - 1) != relatedIndex) {
        setSelected(itemIndex - 1, -1, false);
      }
      drawNotSelected(g, entry.line.userData, itemIndex);
    }
  }

  override protected function drawEntry(entry:TextLineAndDisplayObjectEntry, itemIndex:int, g:Graphics, w:Number, h:Number, x:Number, y:Number):void {
    var skin:IUIComponent = CompositeEntry(entry).components[0].skin;
    skin.x = (x + w) - 2 - 1 - skin.getExplicitOrMeasuredWidth();

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

    const bottomY:Number = _fixedRendererDimension + 0.5;
    const leftX:Number = x + 0.5;
    g.moveTo(0.5, 24.5);
    if (x != 0) {
      g.lineTo(0.5, bottomY);
      g.lineTo(leftX, bottomY);
    }

    drawRect(g, leftX, -1.5, (x + w) - 0.5, bottomY, isLast(itemIndex), false);
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

  // left renderer overlaps right renderer by 1px
  private function drawNotSelected(g:Graphics, w:Number, itemIndex:int):void {
    sharedMatrix.createGradientBox(w, _fixedRendererDimension - 1, Math.PI / 2, 0.5, 0.5);
    g.drawGraphicsData(inactiveStrokeAndFillData);

    var rightX:Number = w + 0.5;
    const topY:Number = 0.5;
    const leftX:Number = 0.5;
    const bottomY:Number = _fixedRendererDimension + 0.5;
    const rightNeighbourIsSelected:Boolean = !isLast(itemIndex) && _selectionModel.isItemSelected(itemIndex + 1);
    if (itemIndex == 0) {
      g.moveTo(leftX, bottomY);
      drawRect(g, leftX, topY, rightX, bottomY, true, rightNeighbourIsSelected);
      g.lineStyle();
      g.lineTo(leftX, bottomY);
    }
    else {
      g.moveTo(1, bottomY);
      g.lineStyle();
      g.lineTo(leftX, topY);
      g.drawGraphicsData(inactiveStrokeData);
      if (rightNeighbourIsSelected) {
        g.lineTo(rightX, topY);
        g.lineStyle();
      }
      else {
        g.lineTo(rightX - ARC_SIZE, topY);
        g.curveTo(rightX, topY, rightX, topY + ARC_SIZE);
      }
      g.lineTo(rightX, bottomY);
      g.lineStyle();
      g.lineTo(1, bottomY);
    }

    g.endFill();

    if (!rightNeighbourIsSelected) {
      if (!isLast(itemIndex)) {
        g.drawGraphicsData(inactiveStrokeData);
        const cx:Number = rightX + 0.5;
        g.moveTo(cx - (ARC_SIZE + 1), topY);
        g.lineTo(cx, topY);
      }

      g.lineStyle(1, IdeaLookAndFeel.BORDER_COLOR);
      g.moveTo(rightX - 1, ARC_SIZE - 1);
      g.lineTo(rightX - 1, _fixedRendererDimension);
    }
  }

  private static function drawRect(g:Graphics, leftX:Number, topY:Number, rightX:Number, bottomY:Number, isLast:Boolean, rightNeighbourIsSelected:Boolean):void {
    g.lineTo(leftX, topY + ARC_SIZE);
    g.curveTo(leftX, topY, leftX + ARC_SIZE, topY);

    if (rightNeighbourIsSelected) {
      g.lineTo(rightX, topY);
      g.lineStyle();
    }
    else {
      g.lineTo(rightX - ARC_SIZE, topY);
      g.curveTo(rightX, topY, rightX, topY + ARC_SIZE);
    }

    if (isLast) {
      g.lineTo(rightX, bottomY);
    }
    else {
      g.lineTo(rightX, bottomY - ARC_SIZE);
      g.curveTo(rightX, bottomY, rightX + ARC_SIZE, bottomY);
    }
  }

  public function tabViewSizeChanged(selectedIndex:int, w:Number, h:Number):void {
    if (_textLineContainer.width == w && _textLineContainer.height == (h + 2 /* selected render height > not-selected render by 2px*/)) {
      return;
    }

    var entry:TextLineAndDisplayObjectEntry = findEntry2(selectedIndex);
    drawSelected(selectedIndex, entry.displayObject.x, entry.line.userData);
  }

  public function clearSelected():void {
    _textLineContainer.graphics.clear();
  }
}
}
