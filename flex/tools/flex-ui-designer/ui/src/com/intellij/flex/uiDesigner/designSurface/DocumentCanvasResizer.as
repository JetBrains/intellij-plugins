package com.intellij.flex.uiDesigner.designSurface {
import com.intellij.flex.uiDesigner.DocumentDisplayManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Sprite;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;

public class DocumentCanvasResizer extends DocumentTool {
  [Embed(source="/canvasKnob.png")]
  private static var knobClass:Class;

  //private static const LINE_COLOR:int = 0x8591a0;
  private static const LINE_COLOR:int = 0xededed;

  private var canvas:Sprite;
  private const knobs:Vector.<Sprite> = new Vector.<Sprite>(3, true);

  override protected function get displayObject():DisplayObject {
    return canvas;
  }

  override protected function createCanvas():DisplayObject {
    canvas = new Sprite();
    canvas.mouseEnabled = false;
    return canvas;
  }

  override protected function doActivate(displayObjectContainer:DisplayObjectContainer, areaLocations:Vector.<Point>, dataContext:DataContext, documentDisplayManager:DocumentDisplayManager):void {
    var g:Graphics = canvas.graphics;
    g.clear();
    
    g.lineStyle(1, LINE_COLOR);
    g.drawRect(0, 0, documentDisplayManager.actualDocumentWidth, documentDisplayManager.actualDocumentHeight);

    var bottom:Sprite = createSide(documentDisplayManager, true);
    bottom.visible = true;

    createSide(documentDisplayManager, false);

    createCorner(documentDisplayManager);
  }

  private function createSide(documentDisplayManager:DocumentDisplayManager, horizontal:Boolean):Sprite {
    var sprite:Sprite = knobs[horizontal ? 0 : 2];
    if (sprite == null) {
      sprite = new Sprite();
      knobs[horizontal ? 0 : 2] = sprite;

      for (var i:int = 0; i < 3; i++) {
        var knob:DisplayObject = new knobClass();
        if (horizontal) {
          knob.x = i * 6;
        }
        else {
          knob.y = i * 6;
        }
        sprite.addChild(knob);
      }

      var a:Number = Math.ceil((documentDisplayManager.actualDocumentWidth - sprite.width) / 2);
      var b:Number = documentDisplayManager.actualDocumentHeight + 6;
      if (horizontal) {
        sprite.y = b;
        sprite.x = a;
      }
      else {
        sprite.x = b;
        sprite.y = a;
      }
      canvas.addChild(sprite);
    }

    return sprite;
  }

  private function createCorner(documentDisplayManager:DocumentDisplayManager):Sprite {
    var sprite:Sprite = knobs[1];
    if (sprite == null) {
      sprite = new Sprite();
      knobs[1] = sprite;

      for (var i:int = 0; i < 3; i++) {
        var knob:DisplayObject = new knobClass();
        if (i != 2) {
          knob.x = 6;
        }

        if (i != 0) {
          knob.y = 6;
        }


        sprite.addChild(knob);
      }

      sprite.x = documentDisplayManager.actualDocumentWidth;
      sprite.y = documentDisplayManager.actualDocumentHeight;
      canvas.addChild(sprite);
    }
    return sprite;
  }
}
}