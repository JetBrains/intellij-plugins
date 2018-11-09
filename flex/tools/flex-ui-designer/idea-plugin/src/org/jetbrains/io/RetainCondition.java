package org.jetbrains.io;

import com.intellij.util.Consumer;
import gnu.trove.TObjectObjectProcedure;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class RetainCondition<K, V extends Identifiable> implements TObjectObjectProcedure<K, V> {
  private final int[] ids;
  @Nullable
  private final Consumer<? super V> removedValueConsumer;

  public RetainCondition(int[] ids) {
    this(ids, null);
  }

  public RetainCondition(int[] ids, @Nullable Consumer<? super V> removedValueConsumer) {
    Arrays.sort(ids);
    this.ids = ids;
    this.removedValueConsumer = removedValueConsumer;
  }

  @Override
  public boolean execute(K key, V value) {
    boolean retained = Arrays.binarySearch(ids, value.getId()) < 0;
    if (!retained && removedValueConsumer != null) {
      removedValueConsumer.consume(value);
    }
    return retained;
  }
}