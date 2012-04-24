package org.jetbrains.io;

import gnu.trove.TObjectObjectProcedure;

import java.util.Arrays;

public class RetainCondition<K, V extends Identifiable> implements TObjectObjectProcedure<K, V> {
  private final int[] ids;

  public RetainCondition(int[] ids) {
    Arrays.sort(ids);
    this.ids = ids;
  }

  @Override
  public boolean execute(K key, V value) {
    return Arrays.binarySearch(ids, value.getId()) < 0;
  }
}
