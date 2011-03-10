package com.intellij.flex.uiDesigner.io;

public interface Marker {
  int getStart();

  int getEnd();

  ByteRange getDataRange();
}
