package com.intellij.flex.uiDesigner.designSurface {
import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Shape;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;

/**
 * target will be painted by ColorTransform — this.transform.colorTransform = new ColorTransform(1, 1, 1, 1, ((ACTIVE_LINE_COLOR >> 16) & 0xff) - ((INACTIVE_LINE_COLOR >> 16) & 0xff), ((ACTIVE_LINE_COLOR >> 8) & 0xff) - ((INACTIVE_LINE_COLOR >> 8) & 0xff), ((ACTIVE_LINE_COLOR >> 0) & 0xff) - ((INACTIVE_LINE_COLOR >> 0) & 0xff))
 */

[Abstract]
public class GridTool implements Tool {
  private var shape:Shape;

  private static const INACTIVE_LINE_COLOR:uint = 0xb0b0b0;
  // active when target (insert component for example)
  private static const ACTIVE_LINE_COLOR:uint = 0xdb9e9e;

  public function activate(displayObjectContainer:DisplayObjectContainer, areaLocations:Vector.<Point>, dataContext:DataContext):void {
    if (shape == null) {
      shape = new Shape();
    }

    if (shape.parent != displayObjectContainer) {
      displayObjectContainer.addChild(shape);
    }

    var location:Point = areaLocations[AreaLocations.BODY];
    shape.x = location.x;
    shape.y = location.y;

    var gridInfo:GridInfo = DesignSurfaceDataKeys.LAYOUT_MANAGER.getData(dataContext).gridInfo;
    drawGrid(gridInfo);
  }

  public function deactivate():void {
    shape.visible = false;
  }

  private function drawGrid(gridInfo:GridInfo):void {
    var columnIntervals:Vector.<Interval> = gridInfo.columnIntervals;
    var rowIntervals:Vector.<Interval> = gridInfo.rowIntervals;

    var g:Graphics = shape.graphics;
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
      const y2:int = rowIntervals[columnIntervals.length - 1].end;
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
