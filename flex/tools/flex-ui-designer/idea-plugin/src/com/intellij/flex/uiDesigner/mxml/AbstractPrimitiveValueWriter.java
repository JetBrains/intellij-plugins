package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

abstract class AbstractPrimitiveValueWriter implements ValueWriter {
  protected int getStyleFlags() {
    return 0;
  }
  
  @Override
  public int write(PrimitiveAmfOutputStream out, boolean isStyle) {
    if (isStyle) {
      out.write(getStyleFlags());
    }
    
    write(out);
    
    return isStyle ? PropertyProcessor.PRIMITIVE_STYLE : PropertyProcessor.PRIMITIVE;
  }

  protected abstract void write(PrimitiveAmfOutputStream out);
}
