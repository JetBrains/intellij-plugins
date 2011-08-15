package com.intellij.flex.uiDesigner.abc;

import java.awt.*;
import java.io.IOException;

abstract class MovieTranscoder extends SwfTranscoder {
  private int bitPos;
  private int bitBuf;

  protected Rectangle bounds;

  protected void syncBits() {
    bitPos = 0;
  }

  protected void decodeRect() throws IOException {
    syncBits();

    bounds = new Rectangle();
    int nBits = readUBits(5);
    bounds.x = readSBits(nBits);
    bounds.width = readSBits(nBits) - bounds.x;
    bounds.y = readSBits(nBits);
    bounds.height = readSBits(nBits) - bounds.y;
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

  protected int readUBits(int numBits) throws IOException {
    if (numBits == 0) {
      return 0;
    }

    int bitsLeft = numBits;
    int result = 0;

    //no value in the buffer - read a byte
    if (bitPos == 0) {
      bitBuf = buffer.get();
      bitPos = 8;
    }

    while (true) {
      int shift = bitsLeft - bitPos;
      if (shift > 0) {
        // Consume the entire buffer
        result |= bitBuf << shift;
        bitsLeft -= bitPos;

        // Get the next byte from the input stream
        bitBuf = buffer.get();
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
