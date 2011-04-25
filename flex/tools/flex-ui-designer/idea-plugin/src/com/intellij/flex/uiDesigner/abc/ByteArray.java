package com.intellij.flex.uiDesigner.abc;

class ByteArray {
  DataBuffer data;
  int start, end, hash;

  void clear() {
    data = null;
    start = 0;
    end = 0;
    hash = 0;
  }

  void init() {
    hash = data.hashCode(start, end);
  }

  public boolean equals(Object obj) {
    if (obj instanceof ByteArray) {
      ByteArray a = (ByteArray)obj;
      return data.same(a.data, start, end, a.start, a.end);
    }
    else {
      return false;
    }
  }

  public int hashCode() {
    return hash;
  }
}
