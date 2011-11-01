package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.flex.uiDesigner.io.InfoMap;

public abstract class EntityListManager<E,I extends Info<E>> {
  protected final InfoMap<E, I> map = new InfoMap<E, I>();

  public void reset() {
    map.clear();
  }
}
