package com.intellij.flex.uiDesigner.abc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

class DataBuffer {
  private final int offset;

  protected byte[] data;
  protected int position;
  protected int size;

  public DataBuffer(byte[] data) {
    this();
    this.data = data;
    size = data.length;
  }

  protected DataBuffer() {
    offset = 0;
  }

  protected DataBuffer(int offset) {
    this.offset = offset;
  }

  public int position() {
    return position;
  }

  public int readU8() {
    return data[offset + position++] & 0xff;
  }

  public int readU8(int index) {
    return data[offset + index] & 0xff;
  }

  public int readU32() {
    int result = readU8();
    if (0 == (result & 0x00000080)) {
      return result;
    }
    result = result & 0x0000007f | readU8() << 7;
    if (0 == (result & 0x00004000)) {
      return result;
    }
    result = result & 0x00003fff | readU8() << 14;
    if (0 == (result & 0x00200000)) {
      return result;
    }
    result = result & 0x001fffff | readU8() << 21;
    if (0 == (result & 0x10000000)) {
      return result;
    }
    return result & 0x0fffffff | readU8() << 28;
  }

  public int readU32(int index) {
    int result = readU8(index++);
    if (0 == (result & 0x00000080)) {
      return result;
    }
    result = result & 0x0000007f | readU8(index++) << 7;
    if (0 == (result & 0x00004000)) {
      return result;
    }
    result = result & 0x00003fff | readU8(index++) << 14;
    if (0 == (result & 0x00200000)) {
      return result;
    }
    result = result & 0x001fffff | readU8(index++) << 21;
    if (0 == (result & 0x10000000)) {
      return result;
    }
    return result & 0x0fffffff | readU8(index) << 28;
  }

  public int readS8() {
    return data[offset + position++];
  }

  public int readS24() {
    return readU8() | (readU8() << 8) | (readS8() << 16);
  }

  public double readDouble() {
    long first = readU8() | (readU8() << 8) | (readU8() << 16) | (readU8() << 24);
    long second = readU8() | (readU8() << 8) | (readU8() << 16) | (readU8() << 24);
    return Double.longBitsToDouble(first & 0xFFFFFFFFL | second << 32);
  }

  public byte[] readBytes(int length) {
    byte[] bytes = new byte[length];
    System.arraycopy(data, offset + position, bytes, 0, length);
    position += length;
    return bytes;
  }

  public String readString(int length) {
    try {
      return new String(data, offset + position, length, "UTF8");
    }
    catch (UnsupportedEncodingException ex) {
      return null;
    }
  }

  public void skip(int length) {
    position += length;
  }

  public void skipEntries(long entries) {
    for (long i = 0; i < entries; ++i) {
      readU32();
    }
  }

  public void seek(int pos) {
    this.position = pos;
  }

  public boolean same(DataBuffer b, int start1, int end1, int start2, int end2) {
    if ((end1 - start1) != (end2 - start2)) {
      return false;
    }

    for (int i = start1, j = start2; i < end1; ) {
      if (data[offset + i] != b.data[b.offset + j]) {
        return false;
      }

      i++;
      j++;
    }

    return b == this;
  }

  public int hashCode(int start, int end) {
    long hash = 1234;

    for (int j = offset + start; j < end; j++) {
      hash ^= data[j];
    }

    return (int)((hash >> 32) ^ hash);
  }

  public void writeTo(OutputStream out) throws IOException {
    out.write(data, offset, size);
  }

  public void writeTo(ByteBuffer buffer, int start, int end) {
    buffer.put(data, offset + start, end - start);
  }

  public void writeTo(ByteBuffer buffer) {
    buffer.put(data, offset, size);
  }

  public int minorVersion() {
    return 16;
  }
}