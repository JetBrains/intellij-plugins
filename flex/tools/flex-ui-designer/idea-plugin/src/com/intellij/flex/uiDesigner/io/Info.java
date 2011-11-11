package com.intellij.flex.uiDesigner.io;

import org.jetbrains.annotations.NotNull;

public class Info<E> implements Identifiable {
  int id = -1;
  protected final E element;

  public Info(@NotNull E element) {
    this.element = element;
  }

  public int getId() {
    return id;
  }

  public E getElement() {
    return element;
  }
}
