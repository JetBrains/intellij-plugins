package com.intellij.flex.uiDesigner.io;

public class ByteRangeMarker implements Marker {
  private final int position;
  private final ByteRange dataRange;
  
  public ByteRangeMarker(int position, ByteRange dataRange) {
    this.position = position;
    this.dataRange = dataRange;
  }
  
  public ByteRange getDataRange() {
    return dataRange;
  }
  
  @Override
  public int getStart() {
    return position;
  }
  
  @Override
  public int getEnd() {
    return position;
  }
}
