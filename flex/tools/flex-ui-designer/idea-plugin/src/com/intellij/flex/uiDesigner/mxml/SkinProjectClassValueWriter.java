package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

class SkinProjectClassValueWriter extends AbstractPrimitiveValueWriter {
  private final int reference;
  private final BaseWriter writer;

  public SkinProjectClassValueWriter(int reference, BaseWriter writer) {
    this.reference = reference;
    this.writer = writer;
  }

  @Override
  protected int getStyleFlags() {
    return 1;
  }

  @Override
  protected void write(PrimitiveAmfOutputStream out, BaseWriter writer) {
    this.writer.writeDocumentFactoryReference(reference);
  }
}
