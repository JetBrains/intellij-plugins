package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.io.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

final class ImageClassPoolGenerator extends AbcEncoder {
  private static byte[] B_ABC;
  private static final int NAME_POS = 12;

  private static void initAbcBlank() throws IOException {
    if (B_ABC == null) {
      InputStream classDefinition = ImageClassPoolGenerator.class.getClassLoader().getResourceAsStream("BitmapAsset.abc");
      try {
        B_ABC = FileUtil.loadBytes(classDefinition);
      }
      finally {
        classDefinition.close();
      }
    }
  }

  public static void generate(ArrayList<Decoder> decoders, int size) throws IOException {
    initAbcBlank();

    if (size < 0 || size > 4095) {
      throw new IllegalArgumentException("size must be >= 0 <= 4095");
    }

    decoders.ensureCapacity(decoders.size() + size);
    decoders.add(new Decoder(new DataBuffer(B_ABC)));

    for (int i = 1, n = size - 1; i < n; i++) {
      final byte[] bAbc = new byte[B_ABC.length];
      System.arraycopy(B_ABC, 0, bAbc, 0, bAbc.length);

      final String index = Integer.toHexString(i);
      int j = 0;
      final int relativeOffset = 3 - index.length();
      final int offset = NAME_POS + relativeOffset;
      while (j < index.length()) {
        bAbc[offset + j] = (byte)index.charAt(j++);
      }

      decoders.add(new Decoder(new DataBuffer(bAbc)));
    }
  }
}