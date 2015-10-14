package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

class ResourceDirectiveValueWriter extends AbstractPrimitiveValueWriter {
  private final String message;

  public ResourceDirectiveValueWriter(String message) {
    this.message = message;
  }

  @Override
  protected void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException {
    if (message == null) {
      writer.stringReference(XmlElementValueProvider.EMPTY);
    }
    else {
      writer.string(message);
    }
  }
}
