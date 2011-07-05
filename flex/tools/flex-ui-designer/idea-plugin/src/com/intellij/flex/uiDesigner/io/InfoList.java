package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import gnu.trove.THashMap;
import gnu.trove.TIntArrayList;
import gnu.trove.TObjectObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfoList<E,I extends Info<E>> {
  private final TIntArrayList freeIndices = new TIntArrayList();
  private final MyHashMap<E,I> elements;
  private final boolean infoIsDisposable;
  private int counter;

  public InfoList() {
    this(false);
  }

  public InfoList(boolean infoIsDisposable) {
    elements = new MyHashMap<E, I>();
    this.infoIsDisposable = infoIsDisposable;
  }

  private class MyHashMap<K, V> extends THashMap<K, V> {
    @Override
    protected void removeAt(int index) {
      if (infoIsDisposable) {
        Disposer.dispose((Disposable)_values[index]);
      }
      freeIndices.add(((Info)_values[index]).getId());
      super.removeAt(index);
    }
  }

  public int add(@NotNull I info) {
    assert info.getId() == -1;

    info.id = freeIndices.isEmpty() ? counter++ : freeIndices.remove(freeIndices.size() - 1);

    elements.put(info.element, info);
    return info.id;
  }

  public void remove(@NotNull E element) {
    elements.remove(element);
  }

  public boolean isEmpty() {
    return elements.isEmpty();
  }

  public void remove(TObjectObjectProcedure<E, I> filter) {
    elements.retainEntries(filter);
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
        return info.element;
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
    if (infoIsDisposable) {
      for (I info : elements.values()) {
        Disposer.dispose((Disposable)info);
      }
    }

    elements.clear();
    counter = 0;
    freeIndices.resetQuick();
  }
}
