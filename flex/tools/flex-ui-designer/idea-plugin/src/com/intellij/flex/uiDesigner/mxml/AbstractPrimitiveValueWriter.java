package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

import static com.intellij.flex.uiDesigner.mxml.PropertyProcessor.PropertyKind;

abstract class AbstractPrimitiveValueWriter implements ValueWriter {
  protected int getStyleFlags() {
    return 0;
  }

  @Override
  public PropertyKind write(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException {
    if (isStyle) {
      out.write(getStyleFlags());
    }

    doWrite(out, writer, isStyle);
    return isStyle ? PropertyKind.PRIMITIVE_STYLE : PropertyKind.PRIMITIVE;
  }

  protected abstract void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException;
}
