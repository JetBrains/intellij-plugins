package org.jetbrains.io;

import gnu.trove.TIntArrayList;

public class IdPool {
  private final TIntArrayList freeIndices = new TIntArrayList();
  private int counter;

  public void dispose(int id) {
    if (id == (counter -  1)) {
      counter--;
    }
    else {
      freeIndices.add(id);
    }
  }

  public void dispose(int[] ids) {
    freeIndices.add(ids);
  }

  public int allocate() {
    return freeIndices.isEmpty() ? counter++ : freeIndices.remove(freeIndices.size() - 1);
  }

  public void clear() {
    counter = 0;
    freeIndices.resetQuick();
  }
}