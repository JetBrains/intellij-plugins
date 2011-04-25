package com.intellij.flex.uiDesigner.abc;

import java.nio.ByteBuffer;

class BufferWrapper extends DataBuffer {
  public BufferWrapper(ByteBuffer buffer, int length) {
    super(buffer.position());
    this.data = buffer.array();
    size = length;
    position = 0;
  }
}