package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.io.ObjectIntHashMap;
import gnu.trove.TObjectIntProcedure;

import java.nio.ByteBuffer;
import java.util.TreeMap;

class ByteArrayPool {
  protected ObjectIntHashMap<ByteArray> map;
  protected com.intellij.util.containers.Stack<ByteArray> wrappers;
  private ByteArray key;

  ByteArrayPool() {
    map = new ObjectIntHashMap<ByteArray>();
    wrappers = new com.intellij.util.containers.Stack<ByteArray>();
    key = newByteArray();
  }

  ByteArray newByteArray() {
    return new ByteArray();
  }

  int store(DataBuffer data, int start, int end) {
    ByteArray a = wrappers.isEmpty() ? null : wrappers.pop();
    if (a == null) {
      a = newByteArray();
    }

    a.clear();
    a.data = data;
    a.start = start;
    a.end = end;
    a.init();
    int index = map.size() + 1;
    map.put(a, index);
    return index;
  }

  int contains(DataBuffer data, int start, int end) {
    key.clear();
    key.data = data;
    key.start = start;
    key.end = end;
    key.hash = 0;
    key.init();

    return map.get(key);
  }

  void writeTo(ByteBuffer b) {
    writeTo(b, 1);
  }

  void writeTo(ByteBuffer b, int f) {
    final TreeMap<Integer, ByteArray> sortedMap = new TreeMap<Integer, ByteArray>();
    map.forEachEntry(new TObjectIntProcedure<ByteArray>() {
      @Override
      public boolean execute(ByteArray a, int b) {
        sortedMap.put(b, a);
        return true;
      }
    });

    writeU32(b, (sortedMap.size() == 0) ? 0 : sortedMap.size() + f);

    for (ByteArray a : sortedMap.values()) {
      a.data.writeTo(b, a.start, a.end);
    }
  }

  protected void writeU32(ByteBuffer buffer, long v) {
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
