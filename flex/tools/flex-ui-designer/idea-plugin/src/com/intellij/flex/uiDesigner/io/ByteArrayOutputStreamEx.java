package com.intellij.flex.uiDesigner.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public final class ByteArrayOutputStreamEx extends AbstractByteArrayOutputStream {
  public ByteArrayOutputStreamEx(int size) {
    super(size);
  }

  public void writeTo(byte[] destination, int destPos) {
    System.arraycopy(buffer, 0, destination, destPos, count);
  }

  public void writeTo(OutputStream out, int offset, int length) throws IOException {
    out.write(buffer, offset, length);
  }

  public void writeTo(OutputStream out) throws IOException {
    out.write(buffer, 0, count);
  }

  public void writeTo(PrimitiveAmfOutputStream out) {
    out.write(buffer, 0, count);
  }

  public void reset() {
    count = 0;
  }

  public byte[] toByteArray() {
    return Arrays.copyOf(buffer, count);
  }
}
