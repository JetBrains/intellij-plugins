package com.intellij.flex.uiDesigner.gef.policy.layout.grid {
import com.intellij.flex.uiDesigner.gef.geometry.Interval;

public interface GridInfo {
  function get columnIntervals():Vector.<Interval>;

  function get rowIntervals():Vector.<Interval>;
}
}
