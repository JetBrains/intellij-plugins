package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

abstract class Binding {
  private MxmlObjectReference target;
  private int propertyName;
  private boolean isStyle;

  void setTarget(MxmlObjectReference target, int propertyName, boolean isStyle) {
    this.target = target;
    this.propertyName = propertyName;
    this.isStyle = isStyle;
  }

  protected abstract int getType();

  void write(PrimitiveAmfOutputStream out, BaseWriter writer, ValueReferenceResolver valueReferenceResolver)
    throws InvalidPropertyException {
    target.write(out, writer, valueReferenceResolver);
    writer.property(propertyName).getOut().write(getType() << 1 | (isStyle ? 1 : 0));
  }

  static final class BindingType {
    final static int MXML_OBJECT = 0;
    final static int MXML_OBJECT_WRAP_TO_ARRAY = 1;
    final static int VARIABLE = 2;
    final static int EXPRESSION = 3;
  }
}