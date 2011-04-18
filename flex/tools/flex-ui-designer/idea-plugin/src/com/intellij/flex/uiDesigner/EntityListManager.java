package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.InfoList;

abstract class EntityListManager<E,I extends InfoList.Info> {
  protected final InfoList<E, I> list = new InfoList<E, I>();

  public void reset() {
    list.clear();
  }
}
