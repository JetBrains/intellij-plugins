package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

class PercentableValueWriter implements ValueWriter {
  private final String value;
  
  public PercentableValueWriter(String value) {
    this.value = value;
  }

  @Override
  public int write(PrimitiveAmfOutputStream out, boolean isStyle) {
    out.writeAmfDouble(value);
    return PropertyProcessor.PRIMITIVE;
  }
}
