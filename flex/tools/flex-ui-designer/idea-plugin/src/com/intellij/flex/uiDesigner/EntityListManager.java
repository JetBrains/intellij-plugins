package com.intellij.flex.uiDesigner;

import org.jetbrains.io.Info;
import org.jetbrains.io.InfoMap;

public abstract class EntityListManager<E,I extends Info<E>> {
  protected final InfoMap<E, I> map = new InfoMap<E, I>();

  public void reset() {
    map.clear();
  }
}
