package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.io.ObjectIntHashMap;

import java.nio.ByteBuffer;

class PoolPart {
  private final ObjectIntHashMap<ByteArray> map;
  private final ByteArray key = createByteArray();
  private final ByteArray[] list;

  int totalBytes;

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
    totalBytes += end - start;
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

  void writeTo(ByteBuffer buffer) {
    writeTo(buffer, 1);
  }

  void writeTo(ByteBuffer buffer, int f) {
    final int size = map.size();
    writeU32(buffer, size == 0 ? 0 : size + f);
    for (int i = 0; i < size; i++) {
      ByteArray a = list[i];
      a.data.writeTo(buffer, a.start, a.end);
      //if (a.data.offset == 13109 && a.start >= 108 && a.end <= 201) {
      //  System.out.print("ff");
      //}

      //if (size > 244) {
      //  int op = a.data.position();
      //  a.data.seek(a.start);
      //  String s = a.data.readString(a.data.readU32());
      //  if (s.length() > 0 && s.charAt(0) == 'E') {
      //    System.out.print("ff");
      //  }
      //
      //  a.data.seek(op);
      //}
    }
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
