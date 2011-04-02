package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.text.StringUtil;

class InjectedArrayOfPrimitivesWriter extends AbstractPrimitiveValueWriter {
  private final JSExpression[] expressions;

  public InjectedArrayOfPrimitivesWriter(JSExpression[] expressions) {
    this.expressions = expressions;
  }

  @Override
  protected void write(PrimitiveAmfOutputStream out, BaseWriter writer) {
    out.write(Amf3Types.ARRAY);
    for (int i = 0, expressionsLength = expressions.length; i < expressionsLength; i++) {
      JSLiteralExpression expression = (JSLiteralExpression)expressions[i];
      if (expression.isNumericLiteral()) {
        writer.write(JSCommonTypeNames.NUMBER_CLASS_NAME);
        out.writeDouble(expression.getText());
      }
      else {
        writer.write(JSCommonTypeNames.STRING_CLASS_NAME);
        out.writeAmfUtf(StringUtil.stripQuotesAroundValue(expression.getText()), false);
      }
    }
    
    out.write(MxmlWriter.EMPTY_CLASS_OR_PROPERTY_NAME);
  }
}