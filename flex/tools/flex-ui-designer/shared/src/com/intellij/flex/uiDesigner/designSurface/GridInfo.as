package com.intellij.flex.uiDesigner.designSurface {
/**
 * Provides information about grid.
 */
public interface GridInfo {
  /**
   * @return the array of {@link Interval} for each column in grid.
   */
  function get columnIntervals():Vector.<Interval>;

  /**
   * @return the array of {@link Interval} for each row in grid.
   */
  function get rowIntervals():Vector.<Interval>;
}
}
