package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.text.StringUtil;

class InjectedArrayOfPrimitivesWriter extends AbstractPrimitiveValueWriter {
  private final JSExpression[] expressions;

  public InjectedArrayOfPrimitivesWriter(JSExpression[] expressions) {
    this.expressions = expressions;
  }

  @Override
  protected void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) {
    out.write(Amf3Types.ARRAY);
    for (int i = 0, expressionsLength = expressions.length; i < expressionsLength; i++) {
      JSLiteralExpression expression = (JSLiteralExpression)expressions[i];
      if (expression.isNumericLiteral()) {
        out.writeAmfDouble(expression.getText());
      }
      else {
        writer.writeString(StringUtil.stripQuotesAroundValue(expression.getText()));
      }
    }
    
    out.write(MxmlWriter.EMPTY_CLASS_OR_PROPERTY_NAME);
  }
}