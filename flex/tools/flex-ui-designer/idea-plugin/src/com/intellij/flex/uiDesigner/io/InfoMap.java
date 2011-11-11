package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.TObjectObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfoMap<E,I extends Info<E>> {
  private final IdPool idPool = new IdPool();
  private final MyHashMap<E,I> elements;
  private final boolean infoIsDisposable;

  public InfoMap() {
    this(false);
  }

  public InfoMap(boolean infoIsDisposable) {
    elements = new MyHashMap<E, I>();
    this.infoIsDisposable = infoIsDisposable;
  }

  private class MyHashMap<K, V> extends THashMap<K, V> {
    @Override
    protected void removeAt(int index) {
      if (infoIsDisposable) {
        Disposer.dispose((Disposable)_values[index]);
      }
      idPool.dispose(((Info)_values[index]).getId());
      super.removeAt(index);
    }
  }

  public int add(@NotNull I info) {
    assert info.getId() == -1;

    info.id = idPool.allocate();

    elements.put(info.element, info);
    return info.id;
  }

  public void remove(@NotNull E element) {
    elements.remove(element);
  }

  public boolean isEmpty() {
    return elements.isEmpty();
  }

  public void remove(int[] ids) {
    elements.retainEntries(new RetainCondition<E, I>(ids));
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

  @Nullable
  public I getNullableInfo(int id) {
    for (I info : elements.values()) {
      if (info.id == id) {
        return info;
      }
    }

    return null;
  }

  @NotNull
  public I getInfo(int id) {
    I result = getNullableInfo(id);
    if (result == null) {
      throw new IllegalArgumentException("Element is not registered for id " + id);
    }

    return result;
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
    idPool.clear();
  }
}
