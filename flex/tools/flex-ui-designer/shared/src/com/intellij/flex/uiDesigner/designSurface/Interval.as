package com.intellij.flex.uiDesigner.designSurface {
public final class Interval {
  public var begin:int;
  public var length:int;

  public function Interval(begin:int, length:int) {
    this.begin = begin;
    this.length = length;
  }

  public function get end():int {
    return begin + length;
  }
}
}
