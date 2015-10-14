package com.intellij.flex.uiDesigner.abc;

import java.nio.ByteBuffer;

abstract class AbcEncoder {
  protected ByteBuffer buffer;

  protected void encodeLongTagHeader(int type, int length) {
    buffer.putShort((short)(type << 6 | 63));
    buffer.putInt(length);
  }

  protected void encodeTagHeader(int code, int length) {
    if (length >= 63) {
      encodeLongTagHeader(code, length);
    }
    else {
      buffer.putShort((short)(code << 6 | length));
    }
  }

  protected final int readUI8() {
    return AbcUtil.readUI8(buffer);
  }

  protected final int readU32() {
    return AbcUtil.readU32(buffer);
  }
}
