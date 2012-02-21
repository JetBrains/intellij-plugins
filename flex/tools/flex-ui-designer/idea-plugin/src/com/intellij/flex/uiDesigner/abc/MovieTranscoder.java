package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.io.IOUtil;
import gnu.trove.TIntArrayList;

import java.awt.*;
import java.io.*;

abstract class MovieTranscoder extends SwfTranscoder {
  private static byte[] SPRITE_SYMBOL_OWN_CLASS_ABC;
  private static byte[] MOVIE_CLIP_SYMBOL_OWN_CLASS_ABC;

  private static final byte[] ROOT_SWF_CLASS_NAME = "flash.display.Sprite".getBytes();
  private static final byte[] SYMBOL_OWN_CLASS_NAME = "_SymbolOwnClass".getBytes();
  protected static final int SYMBOL_CLASS_TAG_LENGTH = 2 /* NumSymbols */ + symbolClassEntryLength(ROOT_SWF_CLASS_NAME) + symbolClassEntryLength(SYMBOL_OWN_CLASS_NAME);
  protected static final int SYMBOL_CLASS_TAG_FULL_LENGTH = recordHeaderLength(SYMBOL_CLASS_TAG_LENGTH) + SYMBOL_CLASS_TAG_LENGTH;

  private int bitPos;
  private int bitBuf;

  protected Rectangle bounds;
  protected DataOutputStream out;
  protected byte[] data;
  
  private static int symbolClassEntryLength(byte[] name) {
    return 2 /* 16-bit character tag ID */ + name.length + 1 /* terminator, 0 */;
  }
  
  protected static int recordHeaderLength(int length) {
    return length < 63 ? 2 : 6;
  }

  protected void transcode(InputStream inputStream, long inputLength, File outFile, boolean writeBounds) throws IOException {
    out = new DataOutputStream(new BufferedOutputStream(transcode(inputStream, inputLength, outFile)));
    data = buffer.array();
    try {
      transcode(writeBounds);
    }
    finally {
      out.close();
      out = null;
    }
  }

  protected abstract void transcode(boolean writeBounds) throws IOException;

  protected void syncBits() {
    bitPos = 0;
  }

  protected void writeSymbolClass(int spriteId) throws IOException {
    buffer.position(0);
    encodeTagHeader(TagTypes.SymbolClass, SYMBOL_CLASS_TAG_LENGTH);
    buffer.putShort((short)2);

    buffer.putShort((short)0);
    buffer.put(ROOT_SWF_CLASS_NAME);
    buffer.put((byte)0);

    buffer.putShort((short)spriteId);
    buffer.put(SYMBOL_OWN_CLASS_NAME);
    buffer.put((byte)0);

    out.write(data, 0, buffer.position());
  }

  protected static byte[] getSymbolOwnClassAbc(short frameCount) throws IOException {
    byte[] symbolOwnClassAbc;
    if (frameCount > 1) {
      if (MOVIE_CLIP_SYMBOL_OWN_CLASS_ABC == null) {
        MOVIE_CLIP_SYMBOL_OWN_CLASS_ABC = IOUtil.getResourceBytes("MSymbolOwnClass.abc");
        MOVIE_CLIP_SYMBOL_OWN_CLASS_ABC[21] = '_'; // replace M => _
      }
      symbolOwnClassAbc = MOVIE_CLIP_SYMBOL_OWN_CLASS_ABC;
    }
    else {
      if (SPRITE_SYMBOL_OWN_CLASS_ABC == null) {
        SPRITE_SYMBOL_OWN_CLASS_ABC = IOUtil.getResourceBytes("SSymbolOwnClass.abc");
        SPRITE_SYMBOL_OWN_CLASS_ABC[21] = '_'; // replace S => _
      }
      symbolOwnClassAbc = SPRITE_SYMBOL_OWN_CLASS_ABC;
    }

    return symbolOwnClassAbc;
  }

  protected void writeMovieBounds() throws IOException {
    out.writeInt(bounds.x);
    out.writeInt(bounds.y);
    out.writeInt(bounds.width);
    out.writeInt(bounds.height);
  }

  protected void writeSparceBytes(TIntArrayList positions, final int start, final int end) throws IOException {
    final int maxI = positions.size() - 1;
    int prevOffset = start;
    int i = 0;
    while (i < maxI) {
      out.write(data, prevOffset, positions.getQuick(i++) - prevOffset);
      prevOffset = positions.getQuick(i++);
    }

    out.write(data, prevOffset, end - prevOffset);
  }

  protected void decodeRect() throws IOException {
    syncBits();

    bounds = new Rectangle();
    final int nBits = readUBits(5);
    bounds.x = readSBits(nBits);
    bounds.width = readSBits(nBits) - bounds.x;
    bounds.y = readSBits(nBits);
    final int i = readSBits(nBits);
    bounds.height = i - bounds.y;
  }

  protected int readSBits(int numBits) throws IOException {
    if (numBits > 32) {
      throw new IOException("Number of bits > 32");
    }

    int num = readUBits(numBits);
    int shift = 32 - numBits;
    // sign extension
    return (num << shift) >> shift;
  }

  protected int readUBits(final int numBits) throws IOException {
    if (numBits == 0) {
      return 0;
    }

    int bitsLeft = numBits;
    int result = 0;

    //no value in the buffer - read a byte
    if (bitPos == 0) {
      bitBuf = readUI8();
      bitPos = 8;
    }

    while (true) {
      final int shift = bitsLeft - bitPos;
      if (shift > 0) {
        // Consume the entire buffer
        result |= bitBuf << shift;
        bitsLeft -= bitPos;

        // Get the next byte from the input stream
        bitBuf = readUI8();
        bitPos = 8;
      }
      else {
        // Consume a portion of the buffer
        result |= bitBuf >> -shift;
        bitPos -= bitsLeft;
        bitBuf &= 0xff >> (8 - bitPos); // mask off the consumed bits
        return result;
      }
    }
  }
}
