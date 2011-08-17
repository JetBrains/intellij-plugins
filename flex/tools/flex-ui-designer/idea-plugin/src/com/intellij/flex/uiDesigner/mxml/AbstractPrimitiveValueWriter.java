package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

abstract class AbstractPrimitiveValueWriter implements ValueWriter {
  protected int getStyleFlags() {
    return 0;
  }

  @Override
  public int write(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException {
    if (isStyle) {
      out.write(getStyleFlags());
    }

    doWrite(out, writer, isStyle);
    return isStyle ? PropertyProcessor.PRIMITIVE_STYLE : PropertyProcessor.PRIMITIVE;
  }

  protected abstract void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException;
}
