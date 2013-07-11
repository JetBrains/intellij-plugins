package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;

class ClassValueWriter extends AbstractPrimitiveValueWriter {
  private final JSClass jsClass;

  public ClassValueWriter(JSClass jsClass) {
    this.jsClass = jsClass;
  }
  
  public JSClass getJsClass() {
    return jsClass;
  }

  @Override
  protected void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) {
    writer.classReference(jsClass.getQualifiedName());
  }
}