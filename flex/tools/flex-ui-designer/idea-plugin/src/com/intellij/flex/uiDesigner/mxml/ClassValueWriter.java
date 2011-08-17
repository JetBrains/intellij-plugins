package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;

class ClassValueWriter extends AbstractPrimitiveValueWriter {
  private final JSClass jsClas;

  public ClassValueWriter(JSClass jsClas) {
    this.jsClas = jsClas;
  }
  
  public JSClass getJsClas() {
    return jsClas;
  }

  @Override
  protected void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) {
    writer.writeClass(jsClas.getQualifiedName());
  }
}