package com.intellij.flex.uiDesigner.migLayout {
import com.intellij.flex.uiDesigner.designSurface.GridInfo;
import com.intellij.flex.uiDesigner.designSurface.geometry.Interval;

import net.miginfocom.layout.Grid;

public class MigLayoutManager {
  private var _gridInfo:GridInfo;
  public function get gridInfo():GridInfo {
    if (_gridInfo == null) {
      _gridInfo = createGridInfo();
    }

    return _gridInfo;
  }

  private function createGridInfo():GridInfo {
    var columnIntervals:Vector.<Interval> = getIntervalsForOrigins(IDEUtil.getColumnSizes(container), 0);
  }

  private static function getIntervalsForOrigins(sizeData:Vector.<Vector.<int>>, startOffset:int):Vector.<Interval> {
    assert(sizeData.length != 0);
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
