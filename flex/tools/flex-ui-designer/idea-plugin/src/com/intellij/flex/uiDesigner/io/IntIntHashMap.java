package com.intellij.flex.uiDesigner.io;

import gnu.trove.TIntIntHashMap;

public final class IntIntHashMap extends TIntIntHashMap {
  @Override
  public int get(int key) {
    int index = index(key);
    return index < 0 ? -1 : _values[index];
  }
}
