package com.intellij.flex.uiDesigner.abc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

final class SwfUtil {
  // FWS, Version 11
  private static final byte[] SWF_HEADER_P1 = {0x46, 0x57, 0x53, 0x0b};
  private static final byte[] SWF_HEADER_P2 = {0x78, 0x00, 0x03, (byte)0xe8, 0x00, 0x00, 0x0b, (byte)0xb8, 0x00,
      // size [Rect 0 0 8000 6000]
      0x00, 0x0c, 0x01, 0x00, // 16bit le frame rate 12, 16bit be frame count 1
      0x44, 0x11, // Tag type=69 (FileAttributes), length=4
      0x08, 0x00, 0x00, 0x00};

  private static final byte[] SWF_FOOTER = {0x40, 0x00, 0x00, 0x00};
  
  public static int getWrapLength() {
    return SWF_HEADER_P1.length + 4 + SWF_HEADER_P2.length + SWF_FOOTER.length;
  }

  public static void header(int length, OutputStream out, ByteBuffer buffer) throws IOException {
    out.write(SWF_HEADER_P1);
    // write length
    buffer.putInt(0, length);
    out.write(buffer.array(), 0, 4);
    out.write(SWF_HEADER_P2);
  }

  public static void footer(OutputStream out) throws IOException {
    out.write(SWF_FOOTER);
  }
}
