package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.psi.JSVariable;

public class VariableBinding extends Binding {
  final JSVariable variable;

  public VariableBinding(JSVariable variable) {
    this.variable = variable;
  }

  @Override
  protected int getType() {
    return BindingType.VARIABLE;
  }

  @Override
  void write(PrimitiveAmfOutputStream out, BaseWriter writer, ValueReferenceResolver valueReferenceResolver)
    throws InvalidPropertyException {
    super.write(out, writer, valueReferenceResolver);

    valueReferenceResolver.getValueReference(variable);
  }
}
