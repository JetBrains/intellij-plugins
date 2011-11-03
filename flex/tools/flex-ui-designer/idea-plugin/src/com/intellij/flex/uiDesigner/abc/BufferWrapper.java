package com.intellij.flex.uiDesigner.abc;

public class BufferWrapper extends DataBuffer {
  public BufferWrapper(byte[] data, int offset, int size) {
    super(offset);
    this.data = data;
    this.size = size;
  }
}