package com.intellij.flex.uiDesigner.designSurface.migLayout {
import com.intellij.flex.uiDesigner.designSurface.GridInfo;
import com.intellij.flex.uiDesigner.designSurface.Interval;

public class MigLayoutGridInfo implements GridInfo {
  public function MigLayoutGridInfo(columnIntervals:Vector.<Interval>, rowIntervals:Vector.<Interval>) {
    _columnIntervals = columnIntervals;
    _rowIntervals = rowIntervals;
  }

  private var _columnIntervals:Vector.<Interval>;
  public function get columnIntervals():Vector.<Interval> {
    return _columnIntervals;
  }

  private var _rowIntervals:Vector.<Interval>;
  public function get rowIntervals():Vector.<Interval> {
    return _rowIntervals;
  }
}
}
