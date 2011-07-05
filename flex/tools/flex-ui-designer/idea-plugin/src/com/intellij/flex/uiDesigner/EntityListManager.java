package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.flex.uiDesigner.io.InfoList;

public abstract class EntityListManager<E,I extends Info<E>> {
  protected final InfoList<E, I> list = new InfoList<E, I>();

  public void reset() {
    list.clear();
  }
}
