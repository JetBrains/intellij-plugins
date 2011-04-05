package com.intellij.flex.uiDesigner.ui {
import com.intellij.flex.uiDesigner.flex.*;

import cocoa.AbstractView;

import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Graphics;
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.geom.Rectangle;

public class DocumentContainer extends AbstractView {
  [Embed(source="/grid.png")]
  private static var gridClass:Class;

  private static const gridBitmapData:BitmapData = Bitmap(new gridClass()).bitmapData;

  private var _documentSysteManager:Sprite;

  public function DocumentContainer(documentSysteManager:Sprite) {
    _documentSysteManager = documentSysteManager;

    _documentSysteManager.addEventListener(MouseEvent.MOUSE_UP, _documentSysteManager_mouseUpHandler, true);
  }

  override protected function createChildren():void {
    _documentSysteManager.x = 16;
    _documentSysteManager.y = 16;
    addDisplayObject(_documentSysteManager);
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    var documentSize:Rectangle = SystemManagerSB(_documentSysteManager).explicitDocumentSize;
    const padding:Number = 15 + 1;
    const totalPadding:Number = padding * 2;
    var actualDocumentWidth:Number = isNaN(documentSize.width) ? w - totalPadding : documentSize.width;
    var actualDocumentHeight:Number = isNaN(documentSize.height) ? h - totalPadding : documentSize.height;
    SystemManagerSB(_documentSysteManager).setActualDocumentSize(actualDocumentWidth, actualDocumentHeight);

    var g:Graphics = graphics;
    g.clear();
    g.beginFill(0xcbcbcb);
    g.drawRect(0, 0, w, h);
    g.endFill();

    drawDocumentBackground(_documentSysteManager.graphics, actualDocumentWidth, actualDocumentHeight);
  }

  private function drawDocumentBackground(g:Graphics, w:Number, h:Number):void {
    g.clear();
    g.lineStyle(1, 0x999999); // intellij idea 0x515151, but it looks bad
    //g.beginBitmapFill(gridBitmapData);
    //g.drawRect(-1, -1, w + 1, h + 1);
    //g.endFill();

    g.beginFill(0xffaaff);
    g.drawRect(-1, -1, w + 1, h + 1);
    g.endFill();
  }

  private function _documentSysteManager_mouseUpHandler(event:MouseEvent):void {
    trace(event);
  }
}
}