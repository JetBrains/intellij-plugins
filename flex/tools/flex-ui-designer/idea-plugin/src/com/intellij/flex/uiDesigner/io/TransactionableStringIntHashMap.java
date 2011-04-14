package com.intellij.flex.uiDesigner.io;

import gnu.trove.TObjectIntIterator;

class TransactionableStringIntHashMap extends ObjectIntHashMap<String> {
  private int lastCommitedTableSize;
  private final int valueOffset;

  TransactionableStringIntHashMap(int initialCapacity) {
    this(initialCapacity, 0);
  }

  TransactionableStringIntHashMap(int initialCapacity, int valueOffset) {
    super(initialCapacity);
    this.valueOffset = valueOffset;
  }

  TransactionableStringIntHashMap() {
    super();
    valueOffset = 0;
  }

  public void start() {
    lastCommitedTableSize = size();
  }

  public void rollback() {
    if (lastCommitedTableSize != size()) {
      final int maxValue = lastCommitedTableSize + valueOffset;
      final int size = size();
      TObjectIntIterator<String> iterator = iterator();
      String[] newStrings = new String[size - lastCommitedTableSize];
      int newStringIndex = 0;
      for (int i = size; i-- > 0; ) {
        iterator.advance();
        if (iterator.value() >= maxValue) {
          newStrings[newStringIndex++] = iterator.key();
        }
      }

      assert newStrings.length == newStringIndex;
      for (String newString : newStrings) {
        remove(newString);
      }
    }
  }
}
