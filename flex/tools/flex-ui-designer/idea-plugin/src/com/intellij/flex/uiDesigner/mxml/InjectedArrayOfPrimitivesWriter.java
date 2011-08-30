package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;

class InjectedArrayOfPrimitivesWriter extends AbstractPrimitiveValueWriter {
  private final JSArrayLiteralExpression arrayLiteralExpression;

  public InjectedArrayOfPrimitivesWriter(JSArrayLiteralExpression arrayLiteralExpression) {
    this.arrayLiteralExpression = arrayLiteralExpression;
  }

  @Override
  protected void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException {
    ExpressionBinding.writeArrayLiteralExpression(arrayLiteralExpression, out, writer, null);
  }
}