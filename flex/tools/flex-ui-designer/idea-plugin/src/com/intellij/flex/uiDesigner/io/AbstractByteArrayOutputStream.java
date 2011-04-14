package com.intellij.flex.uiDesigner.io;

import java.io.OutputStream;
import java.util.Arrays;

public abstract class AbstractByteArrayOutputStream extends OutputStream {
  protected int count;
  protected byte buffer[];

  public AbstractByteArrayOutputStream(int size) {
    buffer = new byte[size];
  }

  public int size() {
    return count;
  }

  public void setPosition(int newPosition) {
    count = newPosition;
  }

  public int allocate(int size) {
    int insertPosition = count;
    // we can't simple increment count — we must erase old buffer content
    for (int i = 0; i < size; i++) {
      buffer[count++] = 0;
    }

    return insertPosition;
  }

  @Override
  public void write(int b) {
    int newCount = count + 1;
    if (newCount > buffer.length) {
      buffer = Arrays.copyOf(buffer, buffer.length << 1);
    }
    buffer[count] = (byte)b;
    count = newCount;
  }

  @Override
  public void write(byte b[], int offset, int length) {
    int newCount = count + length;
    if (newCount > buffer.length) {
      buffer = Arrays.copyOf(buffer, Math.max(buffer.length << 1, newCount));
    }
    System.arraycopy(b, offset, buffer, count, length);
    count = newCount;
  }

  public byte[] getBuffer() {
    return buffer;
  }

  public byte[] getBuffer(int size) {
    int newCount = count + size;
    if (newCount > buffer.length) {
      buffer = Arrays.copyOf(buffer, Math.max(buffer.length << 1, newCount));
    }
    count = newCount;
    return buffer;
  }

  public void moveTo(int position, PrimitiveAmfOutputStream out) {
    out.write(buffer, position, count - position);
    count = position;
  }
}
