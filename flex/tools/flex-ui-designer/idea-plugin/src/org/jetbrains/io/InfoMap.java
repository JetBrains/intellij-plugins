package org.jetbrains.io;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.Consumer;
import gnu.trove.THashMap;
import gnu.trove.TObjectObjectProcedure;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfoMap<E,I extends Info<E>> implements Disposable {
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

  @Override
  public void dispose() {
    if (infoIsDisposable) {
      elements.forEachValue(new TObjectProcedure<I>() {
        @Override
        public boolean execute(I info) {
          ((Disposable)info).dispose();
          return true;
        }
      });
    }
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
    assert info.id == -1;
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

  public void forEach(TObjectProcedure<I> procedure) {
    elements.forEachValue(procedure);
  }

  public void remove(int[] ids) {
    remove(ids, null);
  }

  public void remove(int[] ids, @Nullable Consumer<I> removedValueConsumer) {
    elements.retainEntries(new RetainCondition<>(ids, removedValueConsumer));
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
