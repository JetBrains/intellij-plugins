package com.intellij.flex.uiDesigner.abc;

import java.nio.ByteBuffer;

public final class AbcUtil {
  public static int readUI8(ByteBuffer buffer) {
    return buffer.get() & 0xff;
  }

  public static int readU32(ByteBuffer buffer) {
    int result = readUI8(buffer);
    if ((result & 0x80) == 0) {
      return result;
    }

    result = result & 0x7F | readUI8(buffer) << 7;
    if ((result & 0x4000) == 0) {
      return result;
    }

    result = result & 0x3FFF | readUI8(buffer) << 14;
    if ((result & 0x200000) == 0) {
      return result;
    }

    result = result & 0x1FFFFF | readUI8(buffer) << 21;
    if ((result & 0x10000000) == 0) {
      return result;
    }

    return result & 0xFFFFFFF | readUI8(buffer) << 28;
  }
}
