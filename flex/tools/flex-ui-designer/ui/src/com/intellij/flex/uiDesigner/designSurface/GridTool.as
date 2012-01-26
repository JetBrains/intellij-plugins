package com.intellij.flex.uiDesigner.designSurface {
import flash.display.DisplayObjectContainer;
import flash.display.Shape;
import flash.geom.Point;

[Abstract]
public class GridTool implements Tool {
  private var shape:Shape;
  
  public function GridTool() {
  }

  protected function getGridInfo():GridInfo {
    throw new Error("abstract");
  }

  public function attach(element:Object, toolContainer:ElementToolContainer):void {
  }

  public function detach():void {
  }

  public function activate(displayObjectContainer:DisplayObjectContainer, areaLocations:Vector.<Point>):void {
    if (shape == null) {
      shape = new Shape();
    }

    if (shape.parent != displayObjectContainer) {
      displayObjectContainer.addChild(shape);
    }

    var location:Point = areaLocations[AreaLocations.BODY];
    shape.x = location.x;
    shape.y = location.y;


  }

  public function showGridFeedback():void {
      var gridInfo:GridInfo = getGridInfo();
      var columnIntervals:Vector.<Interval> = gridInfo.columnIntervals;
      var rowIntervals:Vector.<Interval> = gridInfo.rowIntervals;
    }

    private function drawHorizontalLines():void {
      //var y:int =
    }

  public function deactivate():void {
    shape.visible = false;
  }
}
}
