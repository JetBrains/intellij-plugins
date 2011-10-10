package com.intellij.flex.maven;

import java.io.DataOutputStream;
import java.io.PrintStream;

final class MyOutputStream extends DataOutputStream {
  private final FakePrinter printer;

  public MyOutputStream(FakePrinter printer) {
    super(printer);
    System.setOut(printer);
    System.setErr(printer);

    this.printer = printer;
  }

  public void enable() {
    printer.enabled = true;
  }

  public void disable() {
    printer.enabled = false;
  }

  static final class FakePrinter extends PrintStream {
    private Boolean enabled;

    public FakePrinter() {
      super(System.out);
    }

    @Override
    public void write(int b) {
      if (enabled) {
        super.write(b);
      }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
      if (enabled) {
        super.write(buf, off, len);
      }
    }
  }
}

