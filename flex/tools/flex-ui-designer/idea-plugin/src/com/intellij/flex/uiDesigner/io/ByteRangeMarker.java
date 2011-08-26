package com.intellij.flex.uiDesigner.io;

public class ByteRangeMarker extends AbstractMarker {
  private final ByteRange dataRange;

  public ByteRangeMarker(int position, ByteRange dataRange) {
    super(position);
    this.dataRange = dataRange;

    if (dataRange.isUsed()) {
      throw new IllegalStateException("data range already used");
    }
    else {
      dataRange.markAsUsed();
    }
  }

  public ByteRange getDataRange() {
    return dataRange;
  }
}
