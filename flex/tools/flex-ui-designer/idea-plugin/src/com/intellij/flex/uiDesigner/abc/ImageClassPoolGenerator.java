package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.io.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public final class ImageClassPoolGenerator extends AbcEncoder {
  private static byte[] B_ABC;
  private static final int NAME_POS = 12;

  private static void initAbcBlank() throws IOException {
    if (B_ABC == null) {
      InputStream classDefinition = ImageClassPoolGenerator.class.getClassLoader().getResourceAsStream("B.abc");
      try {
        B_ABC = FileUtil.loadBytes(classDefinition);
      }
      finally {
        classDefinition.close();
      }
    }
  }

  public static void generate(FileChannel outFileChannel, int size) throws IOException {
    initAbcBlank();

    if (size < 0 || size > 4095) {
      throw new IllegalArgumentException("size must be >= 0 <= 4095");
    }

    ByteBuffer buffer = ByteBuffer.wrap(B_ABC);
    outFileChannel.write(buffer);
    buffer.clear();

    for (int i = 1, n = size - 1; i < n; i++) {
      String index = Integer.toHexString(i);
      int j = 0;
      final int relativeOffset = 3 - index.length();
      final int offset = NAME_POS + relativeOffset;
      while (j < index.length()) {
        B_ABC[offset + j] = (byte)index.charAt(j++);
      }

      System.arraycopy(B_ABC, offset, B_ABC, 27 + relativeOffset, j);

      outFileChannel.write(buffer);
      buffer.clear();
    }
  }
}