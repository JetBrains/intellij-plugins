package com.intellij.flex.uiDesigner.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class PrimitiveAmfOutputStream extends OutputStream {
  private static final int UINT29_MASK = 0x1FFFFFFF;
  private static final int INT28_MAX_VALUE = 0x0FFFFFFF;
  private static final int INT28_MIN_VALUE = 0xF0000000;

  private AbstractByteArrayOutputStream out;
  
  public PrimitiveAmfOutputStream(@NotNull OutputStream out) {
    this.out = (AbstractByteArrayOutputStream) out;
  }

  public void close() throws IOException {
    flush();
    out.close();
  }

  public void reset() {
    resetSizeAndPosition();
  }

  public void writeTo(PrimitiveAmfOutputStream out) {
    ((ByteArrayOutputStreamEx) this.out).writeTo(out);
  }

  void resetSizeAndPosition() {
    if (out instanceof ByteArrayOutputStreamEx) {
     ((ByteArrayOutputStreamEx) out).reset();
    }
  }
  
  public AbstractByteArrayOutputStream getByteOut() {
    return out;
  }

  public ByteArrayOutputStreamEx getByteArrayOut() {
    return (ByteArrayOutputStreamEx) out;
  }
  
  public BlockDataOutputStream getBlockOut() {
    return (BlockDataOutputStream) out;
  }

  public void write(Enum value) {
    write(value.ordinal());
  }

  // Represent smaller integers with fewer bytes using the most significant bit of each byte. The worst case uses 32-bits
  // to represent a 29-bit number, which is what we would have done with no compression.
  public final void writeUInt29(int v) {
    if (v < 0x80) {
      out.write(v);
    }
    else if (v < 0x4000) {
      int count = out.size();
      final byte[] bytes = out.getBuffer(2);
      bytes[count++] = (byte) (((v >> 7) & 0x7F) | 0x80);
      bytes[count] = (byte) (v & 0x7F);
    }
    else if (v < 0x200000) {
      int count = out.size();
      final byte[] bytes = out.getBuffer(3);
      bytes[count++] = (byte) (((v >> 14) & 0x7F) | 0x80);
      bytes[count++] = (byte) (((v >> 7) & 0x7F) | 0x80);
      bytes[count] = (byte) (v & 0x7F);
    }
    else if (v < 0x40000000) {
      int count = out.size();
      final byte[] bytes = out.getBuffer(4);
      bytes[count++] = (byte) (((v >> 22) & 0x7F) | 0x80);
      bytes[count++] = (byte) (((v >> 15) & 0x7F) | 0x80);
      bytes[count++] = (byte) (((v >> 8) & 0x7F) | 0x80);
      bytes[count] = (byte) (v & 0xFF);
    }
    else {
      throw new IllegalArgumentException("Integer out of range: " + v);
    }
  }
  
  public void writeAmfUtf(String s) {
    writeAmfUTF(s, false);
  }

  public final void writeAmfUTF(CharSequence s, boolean shiftLength) {
    final int strlen = s.length();
    int utflen = 0;
    int c;

    for (int i = 0; i < strlen; i++) {
      c = s.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        utflen++;
      }
      else if (c > 0x07FF) {
        utflen += 3;
      }
      else {
        utflen += 2;
      }
    }

    writeUInt29(shiftLength ? ((utflen << 1) | 1) : utflen);

    int count = out.size();
    final byte[] bytes = out.getBuffer(utflen);

    for (int i = 0; i < strlen; i++) {
      c = s.charAt(i);
      if (c <= 0x007F) {
        bytes[count++] = (byte) c;
      }
      else if (c > 0x07FF) {
        bytes[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        bytes[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        bytes[count++] = (byte) (0x80 | (c & 0x3F));
      }
      else {
        bytes[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        bytes[count++] = (byte) (0x80 | (c & 0x3F));
      }
    }
  }

  public final void writeAmfInt(int v) {
    if (v >= INT28_MIN_VALUE && v <= INT28_MAX_VALUE) {
      write(Amf3Types.INTEGER);
      writeUInt29(v & UINT29_MASK);
    }
    else {
      writeAmfDouble(v);
    }
  }
  
  public void writeAmfInt(String v) {
    writeAmfUInt(Integer.parseInt(v, 10));
  }

  public void writeAmfUInt(int v) {
    writeAmfInt(v < 0 ? (v + 16777216) : v);
  }
  
  @SuppressWarnings({"UnusedDeclaration"})
  public void writeAmfUInt(String v) {
    writeAmfUInt(Integer.parseInt(v, 10));
  }

  public void writeAmfDouble(double v) {
    write(Amf3Types.DOUBLE);
    writeDouble(v);
  }
  
  public final void writeAmfDouble(String v) {
    write(Amf3Types.DOUBLE);
    writeDouble(Double.parseDouble(v));
  }

  public final void write(int b) {
    out.write(b);
  }

  public final void write(byte b[]) {
    out.write(b, 0, b.length);
  }

  public final void write(byte b[], int off, int len) {
    out.write(b, off, len);
  }

  public void flush() throws IOException {
    out.flush();
  }
  
  public void writeAmfBoolean(CharSequence v) {
    write(v.charAt(0) == 't' ? Amf3Types.TRUE : Amf3Types.FALSE);
  }

  public final void write(boolean v) {
    out.write(v ? 1 : 0);
  }

  public final void writeShort(int v) {
    int count = out.size();
    final byte[] bytes = out.getBuffer(2);
    bytes[count++] = (byte) ((v >>> 8) & 0xFF);
    bytes[count] = (byte) (v & 0xFF);
  }
  
  public final void putShort(int v, int position) {
    final byte[] bytes = out.getBuffer();
    bytes[position++] = (byte) ((v >>> 8) & 0xFF);
    bytes[position] = (byte) (v & 0xFF);
  }

  public final void writeInt(int v) {
    int count = out.size();
    final byte[] bytes = out.getBuffer(4);
    bytes[count++] = (byte) ((v >>> 24) & 0xFF);
    bytes[count++] = (byte) ((v >>> 16) & 0xFF);
    bytes[count++] = (byte) ((v >>> 8) & 0xFF);
    bytes[count] = (byte) (v & 0xFF);
  }

  public final void writeLong(long v) {
    int count = out.size();
    final byte[] bytes = out.getBuffer(8);
    bytes[count++] = (byte) (v >>> 56);
    bytes[count++] = (byte) (v >>> 48);
    bytes[count++] = (byte) (v >>> 40);
    bytes[count++] = (byte) (v >>> 32);
    bytes[count++] = (byte) (v >>> 24);
    bytes[count++] = (byte) (v >>> 16);
    bytes[count++] = (byte) (v >>> 8);
    bytes[count] = (byte) (v);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public final void writeFloat(float v) {
    writeInt(Float.floatToIntBits(v));
  }

  public final void writeDouble(double v) {
    writeLong(Double.doubleToLongBits(v));
  }
  
  public final void writeDouble(String v) {
    writeLong(Double.doubleToLongBits(Double.parseDouble(v)));
  }

  public final int size() {
    return out.size();
  }

  public final void writeNotEmptyString(String s) {
    if (s == null) {
      write(0);
    }
    else {
      writeAmfUTF(s, false);
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void moveTo(int dataPosition, PrimitiveAmfOutputStream destination) {
    out.moveTo(dataPosition, destination);
  }
}
