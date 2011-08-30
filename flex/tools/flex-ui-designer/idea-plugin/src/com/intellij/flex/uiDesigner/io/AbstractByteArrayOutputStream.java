package com.intellij.flex.uiDesigner.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

public abstract class AbstractByteArrayOutputStream extends OutputStream implements WritableByteChannel {
  private static final int MAX_BUFFER_SIZE = 2 * 1024 * 1024;
  
  protected int count;
  protected byte buffer[];

  public AbstractByteArrayOutputStream(int size) {
    buffer = new byte[size];
  }

  public int size() {
    return count;
  }

  public void reset() {
    count = 0;
  }

  @Override
  public int write(ByteBuffer byteBuffer) throws IOException {
    final int length = byteBuffer.remaining();
    final int offset = count;
    byteBuffer.get(getBuffer(length), offset, length);
    return length;
  }

  public int write(ByteBuffer byteBuffer, int position) throws IOException {
    final int length = byteBuffer.remaining();
    byteBuffer.get(buffer, position, length);
    return length;
  }

  public void setPosition(int newPosition) {
    count = newPosition;
  }

  int allocate(int size) {
    final int insertPosition = count;
    // we can't simple increment count — we must erase old buffer content
    for (int i = 0; i < size; i++) {
      buffer[count++] = 0;
    }

    return insertPosition;
  }

  int allocateDirty(int size) {
    final int insertPosition = count;
    count += size;
    return insertPosition;
  }

  @Override
  public void write(int b) {
    int newCount = count + 1;
    if (newCount > buffer.length) {
      enlargeBuffer(buffer.length << 1);
    }
    buffer[count] = (byte)b;
    count = newCount;
  }

  @Override
  public void write(byte b[], int offset, int length) {
    int newCount = count + length;
    if (newCount > buffer.length) {
      enlargeBuffer(Math.max(buffer.length << 1, newCount));
    }
    System.arraycopy(b, offset, buffer, count, length);
    count = newCount;
  }

  public final byte[] getBuffer() {
    return buffer;
  }

  public byte[] getBuffer(int size) {
    int newCount = count + size;
    if (newCount > buffer.length) {
      enlargeBuffer(Math.max(buffer.length << 1, newCount));
    }
    count = newCount;
    return buffer;
  }

  private void enlargeBuffer(int newLength) {
    if (buffer.length > MAX_BUFFER_SIZE) {
      throw new IllegalStateException("Buffer is too big");
    }
    buffer = Arrays.copyOf(buffer, newLength);
  }
}
