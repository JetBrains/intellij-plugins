package spark.modules {
import com.intellij.flex.uiDesigner.util.CustomComponentPainter;

import flash.display.Graphics;
import flash.text.engine.TextLine;
import flash.utils.ByteArray;

import mx.core.mx_internal;

import spark.components.ResizeMode;

use namespace mx_internal;

public class ModuleLoader extends FoduleLoader {
  private var statusTextLine:TextLine;
  private var statusText:String;

  override public function loadModule(url:String = null, bytes:ByteArray = null):void {
  }

  override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
    super.updateDisplayList(unscaledWidth, unscaledHeight);

    const w:Number = (resizeMode == ResizeMode.SCALE) ? measuredWidth : unscaledWidth;
    const h:Number = (resizeMode == ResizeMode.SCALE) ? measuredHeight : unscaledHeight;

    var g:Graphics = graphics;
    g.clear();

    if (isNaN(w) || isNaN(h) || w == 0 || h == 0) {
      return;
    }

    const newStatusText:String = "ModuleLoader " + (url == null || url.length == 0 ? "<empty URL>" : url);
    if (newStatusText != statusText) {
      statusText = newStatusText;
      statusTextLine = CustomComponentPainter.createTextLine(newStatusText, statusTextLine, w);
      if (statusTextLine != null && statusTextLine.parent == null) {
        $addChild(statusTextLine);
      }
    }

    if (statusTextLine != null) {
      CustomComponentPainter.layoutTextLine(statusTextLine, w, h);
    }

    CustomComponentPainter.paint(g, w, h);
  }
}
}