package com.intellij.flex.uiDesigner.util {
import flash.display.Graphics;
import flash.text.engine.ElementFormat;
import flash.text.engine.FontDescription;
import flash.text.engine.TextBlock;
import flash.text.engine.TextElement;
import flash.text.engine.TextLine;

public final class CustomComponentPainter {
  private static const textElement:TextElement = new TextElement(null, new ElementFormat(new FontDescription("Lucida Grande, Segoe UI, Sans"), 13, 0xffffff));
  private static const textBlock:TextBlock = new TextBlock(textElement);

  public static function createTextLine(text:String, textLine:TextLine, w:Number):TextLine {
    textElement.text = text;
    if (textLine == null) {
      textLine = textBlock.createTextLine(null, w);
    }
    else {
      textBlock.recreateTextLine(textLine, null, w);
    }

    return textLine;
  }

  public static function paint(g:Graphics, w:Number, h:Number):void {
    g.lineStyle(1, 0xb8b8b8);
    g.drawRect(0.5, 0.5, w - 1, h - 1);

    g.beginFill(0xb8cae5);
    g.lineStyle(1, 0xffffff);
    g.drawRect(1.5, 1.5, w - 3, h - 3);
    g.endFill();
  }

  public static function layoutTextLine(textLine:TextLine, w:Number, h:Number):void {
    textLine.x = (w - textLine.textWidth) * 0.5;
    textLine.y = ((h - textLine.textHeight) * 0.5) + textLine.ascent;
  }
}
}
