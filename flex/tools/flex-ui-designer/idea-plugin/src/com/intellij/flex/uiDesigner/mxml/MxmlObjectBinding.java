package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

class MxmlObjectBinding extends Binding {
  private final String value;
  private final boolean wrapToArray;

  MxmlObjectBinding(String value, boolean wrapToArray) {
    this.value = value;
    this.wrapToArray = wrapToArray;
  }

  @Override
  void write(PrimitiveAmfOutputStream out, BaseWriter writer, ValueReferenceResolver valueReferenceResolver)
    throws InvalidPropertyException {
    super.write(out, writer, valueReferenceResolver);

    valueReferenceResolver.getValueReference(value).write(out, writer, valueReferenceResolver);
  }

  @Override
  protected int getType() {
    return wrapToArray ? BindingType.MXML_OBJECT_WRAP_TO_ARRAY : BindingType.MXML_OBJECT;
  }
}
