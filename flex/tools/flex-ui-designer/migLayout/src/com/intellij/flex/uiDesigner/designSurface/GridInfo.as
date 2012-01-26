package com.intellij.flex.uiDesigner.designSurface {
import com.intellij.flex.uiDesigner.designSurface.geometry.Interval;

public interface GridInfo {
  function get columnIntervals():Vector.<Interval>;

  function get rowIntervals():Vector.<Interval>;
}
}
