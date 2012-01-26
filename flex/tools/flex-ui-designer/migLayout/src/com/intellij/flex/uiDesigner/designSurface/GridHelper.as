package com.intellij.flex.uiDesigner.designSurface {
import com.intellij.flex.uiDesigner.designSurface.geometry.Interval;
import com.intellij.flex.uiDesigner.designSurface.policy.layout.grid.GridInfo;

[Abstract]
public class GridHelper {
  public function GridHelper() {
  }

  protected function getGridInfo():GridInfo {
    throw new Error("abstract");
  }

  public function showGridFeedback():void {
    var gridInfo:GridInfo = getGridInfo();
    var columnIntervals:Vector.<Interval> = gridInfo.columnIntervals;
    var rowIntervals:Vector.<Interval> = gridInfo.rowIntervals;
  }

  private function drawHorizontalLines():void {
    //var y:int =
  }
}
}
