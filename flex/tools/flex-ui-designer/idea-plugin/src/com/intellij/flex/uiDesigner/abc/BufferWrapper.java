package com.intellij.flex.uiDesigner.abc;

import org.jetbrains.annotations.Nullable;

public class BufferWrapper extends DataBuffer {
  @Nullable
  public AbcModifier abcModifier;

  public BufferWrapper(byte[] data, int offset, int size) {
    super(offset);
    this.data = data;
    this.size = size;
  }
}