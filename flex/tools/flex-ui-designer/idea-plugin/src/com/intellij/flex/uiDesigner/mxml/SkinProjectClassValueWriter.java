package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

class SkinProjectClassValueWriter extends AbstractPrimitiveValueWriter {
  private final int reference;

  public SkinProjectClassValueWriter(int reference) {
    this.reference = reference;
  }

  @Override
  protected int getStyleFlags() {
    return StyleFlags.SKIN_IN_PROJECT;
  }

  @Override
  protected void write(PrimitiveAmfOutputStream out, BaseWriter writer) {
    out.writeUInt29(reference); // MxmlReader knows about AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE
  }
}