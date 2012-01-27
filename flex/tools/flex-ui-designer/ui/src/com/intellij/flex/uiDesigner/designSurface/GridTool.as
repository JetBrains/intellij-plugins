package com.intellij.flex.uiDesigner.designSurface {
import com.intellij.flex.uiDesigner.DocumentDisplayManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Shape;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;

/**
 * target will be painted by ColorTransform — this.transform.colorTransform = new ColorTransform(1, 1, 1, 1, ((ACTIVE_LINE_COLOR >> 16) & 0xff) - ((INACTIVE_LINE_COLOR >> 16) & 0xff), ((ACTIVE_LINE_COLOR >> 8) & 0xff) - ((INACTIVE_LINE_COLOR >> 8) & 0xff), ((ACTIVE_LINE_COLOR >> 0) & 0xff) - ((INACTIVE_LINE_COLOR >> 0) & 0xff))
 */

[Abstract]
public class GridTool extends DocumentTool {
  private static const INACTIVE_LINE_COLOR:uint = 0xb0b0b0;
  // active when target (insert component for example)
  private static const ACTIVE_LINE_COLOR:uint = 0xdb9e9e;

  private var canvas:Shape;

  override protected function get displayObject():DisplayObject {
    return canvas;
  }

  override protected function createCanvas():DisplayObject {
    canvas = new Shape();
    return canvas;
  }

  override protected function doActivate(displayObjectContainer:DisplayObjectContainer, areaLocations:Vector.<Point>, dataContext:DataContext, documentDisplayManager:DocumentDisplayManager):void {
    var gridInfo:GridInfo = documentDisplayManager.layoutManager.gridInfo;
    // todo draw only if needed
    drawGrid(gridInfo);
  }

  private function drawGrid(gridInfo:GridInfo):void {
    var columnIntervals:Vector.<Interval> = gridInfo.columnIntervals;
    var rowIntervals:Vector.<Interval> = gridInfo.rowIntervals;

    var g:Graphics = canvas.graphics;
    g.clear();
    g.lineStyle(1, INACTIVE_LINE_COLOR);
    drawHorizontalLines(g, columnIntervals, rowIntervals);
    drawVerticalLines(g, columnIntervals, rowIntervals);
  }

  private static function drawHorizontalLines(g:Graphics, columnIntervals:Vector.<Interval>, rowIntervals:Vector.<Interval>):void {
    var y:int = 0;
    if (columnIntervals.length != 0) {
      const x1:int = 0;
      const x2:int = columnIntervals[columnIntervals.length - 1].end;
      for each (var interval:Interval in rowIntervals) {
        y = interval.begin;
        g.moveTo(x1, y);
        g.lineTo(x2, y);

        y = interval.end;
        g.moveTo(x1, y);
        g.lineTo(x2, y);
      }
    }
  }

  private static function drawVerticalLines(g:Graphics, columnIntervals:Vector.<Interval>, rowIntervals:Vector.<Interval>):void {
    var x:int = 0;
    if (columnIntervals.length != 0) {
      const y1:int = 0;
      const y2:int = rowIntervals[rowIntervals.length - 1].end;
      for each (var interval:Interval in columnIntervals) {
        x = interval.begin;
        g.moveTo(x, y1);
        g.lineTo(x, y2);

        x = interval.end;
        g.moveTo(x, y1);
        g.lineTo(x, y2);
      }
    }
  }
}
}
