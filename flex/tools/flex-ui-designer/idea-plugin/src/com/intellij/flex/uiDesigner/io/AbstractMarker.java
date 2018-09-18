package com.intellij.flex.uiDesigner.io;

import gnu.trove.TLinkableAdaptor;

abstract class AbstractMarker extends TLinkableAdaptor implements Marker {
  private final int position;

  AbstractMarker(int position) {
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
