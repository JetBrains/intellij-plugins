package com.intellij.flex.uiDesigner.io;

import java.io.OutputStream;

public class UnbufferedOutput extends AuditorOutput {
  public final int messageId;

  UnbufferedOutput(OutputStream out, int messageId) {
    super(out);
    this.messageId = messageId;
    written = 0;
  }
}
