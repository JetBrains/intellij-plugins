package com.intellij.flex.uiDesigner.io;

import gnu.trove.TLinkableAdaptor;

public class ByteRange extends TLinkableAdaptor implements Marker {
  private boolean used;

  private final int start;
  private int end;

  private int ownLength = -1;

  public ByteRange(int start) {
    this.start = start;
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

  public int getOwnLength() {
    return ownLength;
  }

  public void setOwnLength(int ownLength) {
    assert this.ownLength == -1 && ownLength > 0;
    this.ownLength = ownLength;
  }

  public boolean isUsed() {
    return used;
  }

  public void markAsUsed() {
    assert !used;
    used = true;
  }
}