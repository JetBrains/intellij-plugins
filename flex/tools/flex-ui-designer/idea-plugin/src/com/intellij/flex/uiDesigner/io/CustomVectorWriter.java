package com.intellij.flex.uiDesigner.io;

public class CustomVectorWriter implements ByteProvider {
  private final TransactionablePrimitiveAmfOutputStream out;
  private int counter = 0;

  public CustomVectorWriter() {
    out = new TransactionablePrimitiveAmfOutputStream(new ByteArrayOutputStreamEx(8 * 1024));
  }

  public void prepareIteration() {
    if (counter != 0 || out.size() > 0) {
      counter = 0;
      out.reset();
    }
  }

  public PrimitiveAmfOutputStream getOutputForIteration() {
    out.start();
    counter++;
    return out;
  }

  public PrimitiveAmfOutputStream getOutputForCustomData() {
    return out;
  }

  public void rollbackLastIteration() {
    counter--;
    out.rollback();
  }

  @Override
  public int size() {
    return IOUtil.sizeOf(counter) + out.size();
  }

  @Override
  public int writeTo(byte[] bytes, int offset) {
    if (counter < 0x80) {
      bytes[offset++] = (byte)counter;
    }
    else if (counter < 0x4000) {
      bytes[offset++] = (byte)(((counter >> 7) & 0x7F) | 0x80);
      bytes[offset++] = (byte)(counter & 0x7F);
    }
    else {
      throw new IllegalArgumentException("Integer out of range: " + counter);
    }

    out.getByteArrayOut().writeTo(bytes, offset);
    return offset + out.size();
  }

  public void writeTo(PrimitiveAmfOutputStream to) {
    to.writeUInt29(counter);
    out.writeTo(to);
  }

  public void writeArrayValueHeader(int length) {
    out.write(Amf3Types.ARRAY);
    out.writeUInt29((length << 1) | 1);
    out.write(1);
  }
}
