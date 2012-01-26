package com.intellij.flex.uiDesigner.ui {
import cocoa.ContentView;
import cocoa.ControlView;
import cocoa.plaf.LookAndFeel;

import com.intellij.flex.uiDesigner.DocumentDisplayManager;
import com.intellij.flex.uiDesigner.DocumentFactory;
import com.intellij.flex.uiDesigner.designSurface.DesignSurfaceDataKeys;
import com.intellij.flex.uiDesigner.designSurface.ToolManager;

import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Sprite;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataContextProvider;
import org.jetbrains.actionSystem.DataKey;
import org.jetbrains.actionSystem.DataManager;

public class DocumentContainer extends ControlView implements DataContextProvider, DataContext {
  private var designerAreaOuterBackgroundColor:int;

  private static const HEADER_SIZE:int = 15;
  private static const CANVAS_INSET:int = HEADER_SIZE + 1 /* line thickness */ + 20;

  private static const AREA_LOCATIONS:Vector.<Point> = new <Point>[new Point(0, HEADER_SIZE + 1), new Point(HEADER_SIZE + 1, 0), new Point(CANVAS_INSET, CANVAS_INSET)];

  private var documentDisplayManager:DocumentDisplayManager;

  // min w/h compute by size of default canvas (the same as in idea/wbpro)
  // user can change size of this canvas, but it is not set document size — only canvas
  // default value the same as in idea (wbpro has another — 450x300)
  private var canvasWidth:int = 500;
  private var canvasHeight:int = 400;

  public function get dataContext():DataContext {
    return this;
  }

  public function DocumentContainer(documentSystemManager:DocumentDisplayManager) {
    this.documentDisplayManager = documentSystemManager;
  }

  override public function getMinimumWidth(hHint:int = -1):int {
    return getPreferredWidth();
  }

  override public function getMinimumHeight(wHint:int = -1):int {
    return getPreferredHeight();
  }

  override public function getPreferredWidth(hHint:int = -1):int {
    return canvasWidth + CANVAS_INSET;
  }

  override public function getPreferredHeight(wHint:int = -1):int {
    return canvasHeight + CANVAS_INSET;
  }

  override public function addToSuperview(displayObjectContainer:DisplayObjectContainer, laf:LookAndFeel, superview:ContentView = null):void {
    super.addToSuperview(displayObjectContainer, laf, superview);

    designerAreaOuterBackgroundColor = laf.getInt("designerAreaOuterBackgroundColor");

    var toolManager:ToolManager = ToolManager(DocumentFactory(documentDisplayManager.documentFactory).module.project.getComponent(ToolManager));
    if (numChildren > 0 && documentDisplayManager == getChildAt(0)) {
      return;
    }

    toolManager.displayObjectContainer = displayObjectContainer;
    toolManager.areaLocations = AREA_LOCATIONS;

    var s:Sprite = Sprite(documentDisplayManager);
    s.x = CANVAS_INSET;
    s.y = CANVAS_INSET;
    addChild(s);
    documentDisplayManager.added();

    if (documentDisplayManager.preferredDocumentWidth != -1) {
      canvasWidth = documentDisplayManager.preferredDocumentWidth;
    }
    if (documentDisplayManager.preferredDocumentHeight != -1) {
      canvasHeight = documentDisplayManager.preferredDocumentHeight;
    }
  }

  override protected function draw(w:int, h:int):void {
    DocumentDisplayManager(documentDisplayManager).setActualDocumentSize(canvasWidth, canvasHeight);

    var g:Graphics = graphics;
    g.clear();

    // all insets the same as in idea

    // draw horizontal and vertical headers background
    // color from wbpro, it is better than idea (because idea is more dark) (I think)
    g.beginFill(designerAreaOuterBackgroundColor);
    g.lineTo(w, 0);
    g.lineTo(w, HEADER_SIZE);
    // color from wbpro, it is better than idea (because idea is more dark) (I think)
    g.lineStyle(1, 0x9f9f9f);
    g.lineTo(HEADER_SIZE, HEADER_SIZE);
    g.lineTo(HEADER_SIZE, h);
    g.lineStyle();
    g.lineTo(0, h);
    g.lineTo(0, 0);
    g.endFill();

    // draw inner white rectangle
    g.beginFill(0xffffff);
    const canvasBackgroundInset:int = HEADER_SIZE + 1;
    g.drawRect(canvasBackgroundInset, canvasBackgroundInset, w - canvasBackgroundInset, h - canvasBackgroundInset);
    g.endFill();
  }

  public function getData(dataKey:DataKey):Object {
    switch (dataKey) {
      case DesignSurfaceDataKeys.LAYOUT_MANAGER:
        return documentDisplayManager.getLayoutManager();

      default:
        return DataManager.instance.getDataContext(parent).getData(dataKey);
    }
  }
}
}