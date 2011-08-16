package com.intellij.flex.uiDesigner.abc;

import java.nio.ByteBuffer;

abstract class AbcEncoder {
  protected ByteBuffer buffer;

  protected void writeSwfString(char v) {
    buffer.put((byte)v);
    buffer.put((byte)0);
  }

  protected void encodeLongTagHeader(int type, int length) {
    buffer.putShort((short)((type << 6) | 63));
    buffer.putInt(length);
  }

  protected void encodeTagHeader(int code, int length) {
    if (length >= 63) {
      encodeLongTagHeader(code, length);
    }
    else {
      buffer.putShort((short)((code << 6) | length));
    }
  }

  protected int readUI8() {
    return buffer.get() & 0xFF;
  }

  protected void writeU30(int v) {
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

  protected int readU32() {
    int result = readUI8();
    if ((result & 0x80) == 0) return result;
    result = result & 0x7F | readUI8() << 7;
    if ((result & 0x4000) == 0) return result;
    result = result & 0x3FFF | readUI8() << 14;
    if ((result & 0x200000) == 0) return result;
    result = result & 0x1FFFFF | readUI8() << 21;
    if ((result & 0x10000000) == 0) return result;
    return result & 0xFFFFFFF | readUI8() << 28;
  }
}
