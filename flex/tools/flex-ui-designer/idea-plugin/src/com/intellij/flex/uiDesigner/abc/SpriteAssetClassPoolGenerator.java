package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.io.IOUtil;

import java.io.IOException;
import java.util.ArrayList;

class SpriteAssetClassPoolGenerator extends AbcEncoder {
  private static byte[] SPRITE_ASSET_ABC;
  private static final int NAME_POS = 18;

  private static void initAbcBlank() throws IOException {
    if (SPRITE_ASSET_ABC == null) {
      SPRITE_ASSET_ABC = IOUtil.getResourceBytes("SpriteAsset.abc");
    }
  }

  public static void generate(ArrayList<Decoder> decoders, int size) throws IOException {
    initAbcBlank();

    if (size < 0 || size > 4095) {
      throw new IllegalArgumentException("size must be >= 0 <= 4095");
    }

    decoders.ensureCapacity(decoders.size() + size);
    decoders.add(new Decoder(new DataBuffer(SPRITE_ASSET_ABC)));

    for (int i = 1, n = size - 1; i < n; i++) {
      final byte[] bAbc = new byte[SPRITE_ASSET_ABC.length];
      System.arraycopy(SPRITE_ASSET_ABC, 0, bAbc, 0, bAbc.length);

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