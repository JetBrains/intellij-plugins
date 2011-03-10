package com.intellij.flex.uiDesigner.io;

public class ByteRange implements Marker {
  private int start;
  private int end;
  
  private final int index;
  private int ownLength = -1;

  public ByteRange(int start, int index) {
    this.start = start;
    this.index = index;
  }

  @Override
  public ByteRange getDataRange() {
    return null;
  }
  
  @Override
  public int getStart() {
    return start;
  }
  
  @Override
  public int getEnd() {
    return end;
  }
  
  public void setEnd(int end) {
    this.end = end;
  }

  public int getIndex() {
    return index;
  }

  public int getOwnLength() {
    return ownLength;
  }

  public void setOwnLength(int ownLength) {
    assert this.ownLength == -1 && ownLength > 0;
    this.ownLength = ownLength;
  }
}