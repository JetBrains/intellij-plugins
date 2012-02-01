package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.Client;
import com.intellij.flex.uiDesigner.AssetCounter;
import com.intellij.flex.uiDesigner.io.AbstractByteArrayOutputStream;
import com.intellij.flex.uiDesigner.io.IOUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class AssetClassPoolGenerator extends AbcEncoder {
  private static byte[] BITMAP_ASSET_ABC;
  private static byte[] SPRITE_ASSET_ABC;
  private static byte[] SPARK_VIEW_ABC;

  private static final int NAME_POS = 12;

  private static void generate(final Client.ClientMethod method, final ArrayList<Decoder> decoders, int size, int counter)
    throws IOException {
    final byte[] abc;
    switch (method) {
      case fillImageClassPool:
        if (SPRITE_ASSET_ABC == null) {
          SPRITE_ASSET_ABC = IOUtil.getResourceBytes("SpriteAsset.abc");
        }
        abc = SPRITE_ASSET_ABC;
        break;
      case fillSwfClassPool:
        if (BITMAP_ASSET_ABC == null) {
          BITMAP_ASSET_ABC = IOUtil.getResourceBytes("BitmapAsset.abc");
        }
        abc = BITMAP_ASSET_ABC;
        break;
      case fillViewClassPool:
        if (SPARK_VIEW_ABC == null) {
          SPARK_VIEW_ABC = IOUtil.getResourceBytes("SparkView.abc");
        }
        abc = SPARK_VIEW_ABC;
        break;

      default:
        throw new IllegalArgumentException("unknown method");
    }

    if (size < 0 || size > 4095) {
      throw new IllegalArgumentException("size must be >= 0 <= 4095");
    }

    if (counter == -1) {
      decoders.ensureCapacity(decoders.size() + size);
      decoders.add(new Decoder(new DataBuffer(abc)));
      counter = 1;
      size--;
    }

    doGenerate(abc, decoders, size, counter);
  }

  private static void doGenerate(final byte[] abc, ArrayList<Decoder> decoders, int size, int counter) throws DecoderException {
    for (int i = 0; i < size; i++, counter++) {
      final byte[] abcN = new byte[abc.length];
      System.arraycopy(abc, 0, abcN, 0, abcN.length);

      final String index = Integer.toHexString(counter);
      int j = 0;
      final int relativeOffset = 3 - index.length();
      final int offset = NAME_POS + relativeOffset;
      while (j < index.length()) {
        abcN[offset + j] = (byte)index.charAt(j++);
      }

      decoders.add(new Decoder(new DataBuffer(abcN)));
    }
  }

  public static void generate(Client.ClientMethod method, int size, AssetCounter allocatedCount, AbstractByteArrayOutputStream out) throws IOException {
    final int start = out.size();
    out.getBuffer(SwfUtil.getWrapHeaderLength());

    ArrayList<Decoder> decoders = new ArrayList<Decoder>(size);
    switch (method) {
      case fillImageClassPool:
        generate(method, decoders, size, allocatedCount.imageCount);
        allocatedCount.imageCount += size;
        break;
      case fillSwfClassPool:
        generate(method, decoders, size, allocatedCount.swfCount);
        allocatedCount.swfCount += size;
        break;
      case fillViewClassPool:
        generate(method, decoders, size, allocatedCount.viewCount);
        allocatedCount.viewCount += size;
        break;

      default:
        throw new IllegalArgumentException("unknown method");
    }

    final ByteBuffer buffer = SwfUtil.mergeDoAbc(decoders).writeDoAbc(out);
    SwfUtil.header(SwfUtil.getWrapLength() + (out.size() - start), out, buffer, start);
    SwfUtil.footer(out);
  }
}