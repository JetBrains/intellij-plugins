package com.intellij.flex.uiDesigner.io;

import gnu.trove.TLinkable;

public interface Marker extends TLinkable {
  int getStart();

  int getEnd();
}
