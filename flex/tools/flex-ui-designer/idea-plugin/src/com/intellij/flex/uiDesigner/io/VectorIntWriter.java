package com.intellij.flex.uiDesigner.io;
public class VectorIntWriter implements ByteProvider {
  private final PrimitiveAmfOutputStream buffer = new PrimitiveAmfOutputStream(new ByteArrayOutputStreamEx(128));

  public void prepareIteration() {
    buffer.reset();
  }

  @Override
  public int size() {
    int size = buffer.size();
    if (size == 0) {
      return 1;
    }
    else {
      return 2 + IOUtil.sizeOf(((size / 4) << 1) | 1) + size;
    }
  }

  public void write(int v) {
    buffer.writeInt(v);
  }

  @Override
  public int writeTo(byte[] bytes, int offset) {
    int size = buffer.size();
    if (size == 0) {
      bytes[offset++] = Amf3Types.NULL;
      return offset;
    }

    bytes[offset++] = Amf3Types.VECTOR_INT;
    int vLength = (size / 4) << 1;
    if (vLength < 0x80) {
      bytes[offset++] = (byte)vLength;
    }
    else if (vLength < 0x4000) {
      bytes[offset++] = (byte)(((vLength >> 7) & 0x7F) | 0x80);
      bytes[offset++] = (byte)(vLength & 0x7F);
    }
    else if (vLength < 0x200000) {
      bytes[offset++] = (byte)(((vLength >> 14) & 0x7F) | 0x80);
      bytes[offset++] = (byte)(((vLength >> 7) & 0x7F) | 0x80);
      bytes[offset++] = (byte)(vLength & 0x7F);
    }
    else if (vLength < 0x40000000) {
      bytes[offset++] = (byte)(((vLength >> 22) & 0x7F) | 0x80);
      bytes[offset++] = (byte)(((vLength >> 15) & 0x7F) | 0x80);
      bytes[offset++] = (byte)(((vLength >> 8) & 0x7F) | 0x80);
      bytes[offset++] = (byte)(vLength & 0xFF);
    }
    else {
      throw new IllegalArgumentException("Integer out of range: " + vLength);
    }

    bytes[offset++] = 1;
    buffer.getByteArrayOut().writeTo(bytes, offset++);

    return offset;
  }
}
