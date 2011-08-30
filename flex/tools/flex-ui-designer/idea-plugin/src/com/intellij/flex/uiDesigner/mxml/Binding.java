package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

abstract class Binding {
  private int target;
  private int propertyName;
  private boolean isStyle;

  void setTarget(int target, int propertyName, boolean isStyle) {
    this.target = target;
    this.propertyName = propertyName;
    this.isStyle = isStyle;
  }

  protected abstract int getType();

  void write(PrimitiveAmfOutputStream out, BaseWriter writer,
             ValueReferenceResolver valueReferenceResolver) throws InvalidPropertyException {
    out.writeUInt29(target);
    out.writeUInt29(propertyName);
    out.write(getType() << 1 | (isStyle ? 1 : 0));
  }

  static final class BindingType {
    final static int MXML_OBJECT = 0;
    final static int VARIABLE = 1;
    final static int EXPRESSION = 2;
  }
}