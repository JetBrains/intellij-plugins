package com.intellij.flex.uiDesigner.abc;

import com.google.common.base.Charsets;
import org.jetbrains.annotations.TestOnly;

import java.nio.ByteBuffer;

class DataBuffer {
  protected final int offset;

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

  @TestOnly
  public String readString(int length) {
    return new String(data, offset + position, length, Charsets.UTF_8);
  }

  public void skip(int length) {
    position += length;
  }

  public void skipEntries(int entries) {
    for (int i = 0; i < entries; i++) {
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

    end1 += offset;
    for (int i = start1 + offset, j = start2 + b.offset; i < end1;) {
      //noinspection AssignmentToForLoopParameter
      if (data[i++] != b.data[j++]) {
        return false;
      }
    }

    return true;
  }

  public int hashCode(int start, int end) {
    long hash = 1234;
    end += offset;
    for (int i = offset + start; i < end; i++) {
      hash ^= data[i];
    }

    return (int)((hash >> 32) ^ hash);
  }

  public final void writeTo(ByteBuffer buffer, int start, int end) {
    buffer.put(data, offset + start, end - start);
  }
}