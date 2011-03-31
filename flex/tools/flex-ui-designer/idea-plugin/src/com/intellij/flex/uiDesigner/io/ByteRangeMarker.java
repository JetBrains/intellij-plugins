package com.intellij.flex.uiDesigner.io;

public class ByteRangeMarker extends AbstractMarker {
  private final ByteRange dataRange;

  public ByteRangeMarker(int position, ByteRange dataRange) {
    super(position);
    this.dataRange = dataRange;
  }

  public ByteRange getDataRange() {
    return dataRange;
  }
}
