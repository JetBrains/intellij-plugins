package com.intellij.flex.uiDesigner.abc;

public interface PlaceObjectFlags {
  static int HAS_CLIP_ACTION = 1 << 7;
  static int HAS_CHARACTER = 1 << 1;
  static int HAS_MATRIX = 1 << 2;
  static int HAS_COLOR_TRANSFORM = 1 << 3;
  static int HAS_RATIO = 1 << 4;
  static int HAS_NAME = 1 << 5;
  static int HAS_CLIP_DEPTH = 1 << 6;
}
