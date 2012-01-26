package com.intellij.flex.uiDesigner.designSurface.migLayout {
import com.intellij.flex.uiDesigner.designSurface.GridInfo;
import com.intellij.flex.uiDesigner.designSurface.Interval;
import com.intellij.flex.uiDesigner.designSurface.LayoutManager;

import net.miginfocom.layout.Grid;
import net.miginfocom.layout.IDEUtil;

import org.jetbrains.migLayout.flex.MigLayout;

public class MigLayoutManager implements LayoutManager {
  private var migLayout:MigLayout;

  public function MigLayoutManager(migLayout:MigLayout) {
    this.migLayout = migLayout;

  }

  private var _gridInfo:GridInfo;
  public function get gridInfo():GridInfo {
    if (_gridInfo == null) {
      _gridInfo = createGridInfo();
    }

    return _gridInfo;
  }

  private function createGridInfo():GridInfo {
    var columnIntervals:Vector.<Interval> = getIntervalsForOrigins(IDEUtil.getColumnSizes(migLayout._ideUtil_grid), 0);
    var rowIntervals:Vector.<Interval> = getIntervalsForOrigins(IDEUtil.getRowSizes(migLayout._ideUtil_grid), 0);
    return new MigLayoutGridInfo(columnIntervals, rowIntervals);
  }

  private static function getIntervalsForOrigins(sizeData:Vector.<Vector.<int>>, startOffset:int):Vector.<Interval> {
    if (sizeData.length == 0) {
      throw new Error("sizeData cannot be null");
    }

    var intervals:Vector.<Interval> = new Vector.<Interval>();
    var n:int = 0;
    var begin:int = startOffset;
    var indices:Vector.<int> = sizeData[0];
    var sizes:Vector.<int> = sizeData[1];
    for (var index:int = 0; index < indices.length; index++) {
      const size:int = sizes[2 * index + 1];
      begin += sizes[2 * index];
      if (Math.abs(indices[index]) < Grid.MAX_GRID) {
        intervals[n++] = new Interval(begin, size);
      }
      begin += size;
    }

    intervals.fixed = true;
    return intervals;
  }
}
}
