package com.intellij.flex.uiDesigner.io;

import java.io.IOException;
import java.io.OutputStream;

class AuditorOutput extends OutputStream {
  private final OutputStream out;
  public int written = -1;

  public AuditorOutput(OutputStream out) {
    this.out = out;
  }

  @Override
  public void write(int b) throws IOException {
    if (written != -1) {
      written++;
    }

    out.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    if (written != -1) {
      written += b.length;
    }

    out.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (written != -1) {
      written += len;
    }

    out.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    out.flush();
  }

  @Override
  public void close() throws IOException {
    out.close();
  }
}
