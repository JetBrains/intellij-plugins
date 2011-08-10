package com.intellij.flex.uiDesigner.abc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

class WritableDataBuffer extends DataBuffer {
  WritableDataBuffer(int preferredSize) {
    preferredSize = preferredSize <= 0 ? 1000 : preferredSize;
    data = new byte[preferredSize];
    size = 0;
    position = 0;
  }

  public void writeTo(WritableByteChannel channel) throws IOException {
    channel.write(ByteBuffer.wrap(data, offset, size));
  }

  public void clear() {
    position = 0;
    size = 0;
  }

  public int size() {
    return size;
  }

  public void delete(int count) {
    size -= count;
  }

  private void resize(int increment) {
    if (size + increment > data.length) {
      byte[] temp = new byte[data.length * 3 / 2 + 1];
      System.arraycopy(data, 0, temp, 0, data.length);
      data = temp;
    }
  }

  public void write(DataBuffer b) {
    write(b.data, b.size);
  }

  public void write(byte[] bytes) {
    write(bytes, bytes.length);
  }

  public void write(byte[] bytes, int length) {
    resize(length);
    System.arraycopy(bytes, 0, data, size, length);
    size += length;
  }

  public void writeU8(int v) {
    resize(1);
    data[size++] = (byte)v;
  }

  public int copyU8(DataBuffer dataBuffer) {
    resize(1);
    return data[size++] = (byte)dataBuffer.readU8();
  }

  public void writeU8(int pos, int v) {
    data[pos] = (byte)v;
  }

  public void writeS24(int v) {
    writeU24(v);
  }

  public void writeS24(int pos, int v) {
    data[pos] = (byte)v;
    data[pos + 1] = (byte)(v >> 8);
    data[pos + 2] = (byte)(v >> 16);
  }

  public void writeU24(int v) {
    resize(3);
    data[size++] = (byte)v;
    data[size++] = (byte)(v >> 8);
    data[size++] = (byte)(v >> 16);
  }

  public void writeU32(int v) {
    if (v < 128 && v > -1) {
      resize(1);
      data[size++] = (byte)v;
    }
    else if (v < 16384 && v > -1) {
      resize(2);
      data[size++] = (byte)((v & 0x7F) | 0x80);
      data[size++] = (byte)((v >> 7) & 0x7F);
    }
    else if (v < 2097152 && v > -1) {
      resize(3);
      data[size++] = (byte)((v & 0x7F) | 0x80);
      data[size++] = (byte)((v >> 7) | 0x80);
      data[size++] = (byte)((v >> 14) & 0x7F);
    }
    else if (v < 268435456 && v > -1) {
      resize(4);
      data[size++] = (byte)((v & 0x7F) | 0x80);
      data[size++] = (byte)(v >> 7 | 0x80);
      data[size++] = (byte)(v >> 14 | 0x80);
      data[size++] = (byte)((v >> 21) & 0x7F);
    }
    else {
      resize(5);
      data[size++] = (byte)((v & 0x7F) | 0x80);
      data[size++] = (byte)(v >> 7 | 0x80);
      data[size++] = (byte)(v >> 14 | 0x80);
      data[size++] = (byte)(v >> 21 | 0x80);
      data[size++] = (byte)((v >> 28) & 0x0F);
    }
  }

  public void close() {
    position = 0;
  }
}
