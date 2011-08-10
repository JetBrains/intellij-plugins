package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.io.AbstractByteArrayOutputStream;
import com.intellij.flex.uiDesigner.io.IOUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class AssetClassPoolGenerator extends AbcEncoder {
  private static byte[] BITMAP_ASSET_ABC;
  private static byte[] SPRITE_ASSET_ABC;

  private static final int NAME_POS = 12;

  public static void generateSprite(ArrayList<Decoder> decoders, int size) throws IOException {
    if (SPRITE_ASSET_ABC == null) {
      SPRITE_ASSET_ABC = IOUtil.getResourceBytes("SpriteAsset.abc");
    }

    generate(SPRITE_ASSET_ABC, decoders, size);
  }

  public static void generateBitmap(ArrayList<Decoder> decoders, int size) throws IOException {
    if (BITMAP_ASSET_ABC == null) {
      BITMAP_ASSET_ABC = IOUtil.getResourceBytes("BitmapAsset.abc");
    }

    generate(BITMAP_ASSET_ABC, decoders, size);
  }

  private static void generate(byte[] abc0, ArrayList<Decoder> decoders, int size) throws DecoderException {
    if (size < 0 || size > 4095) {
      throw new IllegalArgumentException("size must be >= 0 <= 4095");
    }

    decoders.add(new Decoder(new DataBuffer(abc0)));

    for (int i = 1, n = size - 1; i < n; i++) {
      final byte[] abcN = new byte[abc0.length];
      System.arraycopy(abc0, 0, abcN, 0, abcN.length);

      final String index = Integer.toHexString(i);
      int j = 0;
      final int relativeOffset = 3 - index.length();
      final int offset = NAME_POS + relativeOffset;
      while (j < index.length()) {
        abcN[offset + j] = (byte)index.charAt(j++);
      }

      decoders.add(new Decoder(new DataBuffer(abcN)));
    }
  }

  public static void generateBitmap(int size, AbstractByteArrayOutputStream out) throws IOException {
    final int start = out.size();
    out.getBuffer(SwfUtil.getWrapHeaderLength());

    ArrayList<Decoder> decoders = new ArrayList<Decoder>(size);
    generateBitmap(decoders, size);
    final ByteBuffer buffer = SwfUtil.mergeDoAbc(decoders).writeDoAbc(out);

    SwfUtil.header(SwfUtil.getWrapLength() + (out.size() - start), out, buffer, start);
    SwfUtil.footer(out);
  }
}