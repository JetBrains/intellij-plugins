package com.intellij.flex.uiDesigner.io;

import gnu.trove.THashMap;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfoList<E,I extends InfoList.Info> {
  private final THashMap<E,I> elements = new THashMap<E,I>();

  private int counter;
  private final TIntArrayList freeIndices = new TIntArrayList();
  
  public int add(@NotNull I info) {
    assert info.id == -1;

    info.id = freeIndices.isEmpty() ? counter++ : freeIndices.remove(freeIndices.size() - 1);
    //noinspection unchecked
    elements.put((E)info.element, info);
    return info.id;
  }

  public void remove(@NotNull E element) {
    freeIndices.add(getInfo(element).id);
    elements.remove(element);
  }

  public boolean contains(E element) {
    return elements.containsKey(element);
  }

  public boolean contains(I info) {
    return info.id != -1;
  }

  @NotNull
  public E getElement(int id) {
    for (I info : elements.values()) {
      if (info.id == id) {
        //noinspection unchecked
        return (E)info.element;
      }
    }

    throw new IllegalArgumentException("Element is not registered for id " + id);
  }

  @NotNull
  public I getInfo(E element) {
    return elements.get(element);
  }

  @Nullable
  public I getNullableInfo(E element) {
    return elements.get(element);
  }

  @NotNull
  public I getInfo(int id) {
    for (I info : elements.values()) {
      if (info.id == id) {
        return info;
      }
    }

    throw new IllegalArgumentException("Element is not registered for id " + id);
  }

  public int getId(E element) {
    return getInfo(element).id;
  }

  public void clear() {
    elements.clear();
    counter = 0;
    freeIndices.resetQuick();
  }

  public static class Info<E> {
    private int id = -1;
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
}
