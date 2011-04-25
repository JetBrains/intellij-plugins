package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.io.ObjectIntHashMap;

import java.nio.ByteBuffer;

class PoolPart {
  private final ObjectIntHashMap<ByteArray> map;
  private final ByteArray key = createByteArray();

  private final ByteArray[] list;

  PoolPart(int poolPartLength) {
    map = new ObjectIntHashMap<ByteArray>(poolPartLength);
    list = new ByteArray[poolPartLength - 1];
  }

  ByteArray createByteArray() {
    return new ByteArray();
  }

  int store(DataBuffer data, int start, int end) {
    ByteArray a = createByteArray();
    a.clear();
    a.data = data;
    a.start = start;
    a.end = end;
    a.init();
    int index = map.size() + 1;
    map.put(a, index);
    list[index - 1] = a;
    return index;
  }

  int contains(DataBuffer data, int start, int end) {
    key.clear();
    key.data = data;
    key.start = start;
    key.end = end;
    key.init();

    return map.get(key);
  }

  void writeTo(ByteBuffer b) {
    writeTo(b, 1);
  }

  void writeTo(ByteBuffer b, int f) {
    final long time = System.currentTimeMillis();
    final int size = map.size();
    writeU32(b, size == 0 ? 0 : size + f);
    for (int i = 0; i < size; i++) {
      ByteArray a = list[i];
      a.data.writeTo(b, a.start, a.end);
    }
   
    System.out.print("\nf: ");
    System.out.print(System.currentTimeMillis() - time);
  }

    static void writeU32(ByteBuffer buffer, long v) {
    if (v < 128 && v >= 0) {
      buffer.put((byte)v);
    }
    else if (v < 16384 && v >= 0) {
      buffer.put((byte)(v & 0x7F | 0x80));
      buffer.put((byte)(v >> 7));
    }
    else if (v < 2097152 && v >= 0) {
      buffer.put((byte)(v & 0x7F | 0x80));
      buffer.put((byte)(v >> 7 | 0x80));
      buffer.put((byte)(v >> 14));
    }
    else if (v < 268435456 && v >= 0) {
      buffer.put((byte)(v & 0x7F | 0x80));
      buffer.put((byte)(v >> 7 | 0x80));
      buffer.put((byte)(v >> 14 | 0x80));
      buffer.put((byte)(v >> 21));
    }
    else {
      buffer.put((byte)(v & 0x7F | 0x80));
      buffer.put((byte)(v >> 7 | 0x80));
      buffer.put((byte)(v >> 14 | 0x80));
      buffer.put((byte)(v >> 21 | 0x80));
      buffer.put((byte)(v >> 28));
    }
  }
}
