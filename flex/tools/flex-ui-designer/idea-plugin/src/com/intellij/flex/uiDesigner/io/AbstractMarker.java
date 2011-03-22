package com.intellij.flex.uiDesigner.io;

public abstract class AbstractMarker implements Marker {
  private final int position;

  public AbstractMarker(int position) {
    this.position = position;
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
