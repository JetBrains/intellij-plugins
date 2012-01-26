package com.intellij.flex.uiDesigner.ui {
import cocoa.ContentView;
import cocoa.ControlView;
import cocoa.plaf.LookAndFeel;

import com.intellij.flex.uiDesigner.DocumentDisplayManager;

import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Sprite;

public class DocumentContainer extends ControlView {
  private var designerAreaOuterBackgroundColor:int;

  private static const HEADER_SIZE:int = 15;
  private static const CANVAS_INSET:int = HEADER_SIZE + 1 /* line thickness */ + 20;

  private var documentSystemManager:Sprite;

  // min w/h compute by size of default canvas (the same as in idea/wbpro)
  // user can change size of this canvas, but it is not set document size — only canvas
  // default value the same as in idea (wbpro has another — 450x300)
  private var canvasWidth:int = 500;
  private var canvasHeight:int = 400;

  public function DocumentContainer(documentSystemManager:Sprite) {
    this.documentSystemManager = documentSystemManager;
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

    documentSystemManager.x = CANVAS_INSET;
    documentSystemManager.y = CANVAS_INSET;
    addChild(documentSystemManager);
    DocumentDisplayManager(documentSystemManager).added();

    var documentSize:DocumentDisplayManager = DocumentDisplayManager(documentSystemManager);
    if (documentSize.preferredDocumentWidth != -1) {
      canvasWidth = documentSize.preferredDocumentWidth;
    }
    if (documentSize.preferredDocumentHeight != -1) {
      canvasHeight = documentSize.preferredDocumentHeight;
    }
  }

  override protected function draw(w:int, h:int):void {
    DocumentDisplayManager(documentSystemManager).setActualDocumentSize(canvasWidth, canvasHeight);

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
    g.drawRect(HEADER_SIZE, HEADER_SIZE, w - HEADER_SIZE, h - HEADER_SIZE);
    g.endFill();
  }
}
}