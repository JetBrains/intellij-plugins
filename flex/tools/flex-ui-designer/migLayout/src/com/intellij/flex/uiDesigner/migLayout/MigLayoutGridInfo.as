package com.intellij.flex.uiDesigner.migLayout {
import com.intellij.flex.uiDesigner.designSurface.geometry.Interval;
import com.intellij.flex.uiDesigner.designSurface.policy.layout.grid.GridInfo;

public class MigLayoutGridInfo implements GridInfo {
  public function MigLayoutGridInfo() {
  }

  public function get columnIntervals():Vector.<Interval> {
    return null;
  }

  public function get rowIntervals():Vector.<Interval> {
    return null;
  }
}
}
