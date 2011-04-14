package com.intellij.flex.uiDesigner.io;

import java.io.OutputStream;

class TransactionableAmfOutputStream extends AmfOutputStream {
  TransactionableAmfOutputStream(OutputStream out) {
    super(out);
  }

  private int lastCommitedPosition;

  public void start() {
    lastCommitedPosition = size();

    if (stringTable != null) {
      stringTable.start();
    }
    if (traitsTable != null) {
      traitsTable.start();
    }
  }

  public void rollback() {
    out.setPosition(lastCommitedPosition);
    lastCommitedPosition = -1;

    if (stringTable != null) {
      stringTable.rollback();
    }
    if (traitsTable != null) {
      traitsTable.rollback();
    }
  }
}
