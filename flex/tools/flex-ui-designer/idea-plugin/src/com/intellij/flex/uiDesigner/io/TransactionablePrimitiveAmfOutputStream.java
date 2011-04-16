package com.intellij.flex.uiDesigner.io;

import java.io.OutputStream;

class TransactionablePrimitiveAmfOutputStream extends PrimitiveAmfOutputStream {
  TransactionablePrimitiveAmfOutputStream(OutputStream out) {
    super(out);
  }

  private int lastCommitedPosition;

  public void start() {
    lastCommitedPosition = size();
  }

  public void rollback() {
    out.setPosition(lastCommitedPosition);
    lastCommitedPosition = -1;
  }
}
