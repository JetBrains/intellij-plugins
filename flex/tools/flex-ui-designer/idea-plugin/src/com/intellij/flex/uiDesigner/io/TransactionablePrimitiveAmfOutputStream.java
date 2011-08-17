package com.intellij.flex.uiDesigner.io;

class TransactionablePrimitiveAmfOutputStream extends PrimitiveAmfOutputStream {
  TransactionablePrimitiveAmfOutputStream(AbstractByteArrayOutputStream out) {
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
