package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.AssetCounter;
import com.intellij.flex.uiDesigner.io.AbstractByteArrayOutputStream;
import com.intellij.flex.uiDesigner.io.IOUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class ClassPoolGenerator extends AbcEncoder {
  public enum Kind {
    IMAGE, SWF, SPARK_VIEW
  }

  private static final byte[][] ABC = new byte[3][];
  private static final int NAME_POS = 12;

  private static void generate(ClassPoolGenerator.Kind kind, final ArrayList<Decoder> decoders, int size, int counter)
    throws IOException {
    byte[] abc = ABC[kind.ordinal()];
    if (abc == null) {
      abc = IOUtil.getResourceBytes(kind.name().toLowerCase() + "ClassTemplate.abc");
      ABC[kind.ordinal()] = abc;
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

  public static void generate(ClassPoolGenerator.Kind kind, int size, AssetCounter allocatedCount, AbstractByteArrayOutputStream out) throws IOException {
    final int start = out.size();
    out.getBuffer(SwfUtil.getWrapHeaderLength());

    ArrayList<Decoder> decoders = new ArrayList<Decoder>(size);
    switch (kind) {
      case IMAGE:
        generate(kind, decoders, size, allocatedCount.imageCount);
        allocatedCount.imageCount += size;
        break;
      case SWF:
        generate(kind, decoders, size, allocatedCount.swfCount);
        allocatedCount.swfCount += size;
        break;
      case SPARK_VIEW:
        generate(kind, decoders, size, allocatedCount.viewCount);
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