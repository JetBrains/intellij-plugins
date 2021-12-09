package com.jetbrains.lang.dart.ide.template.macro;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartSuggestVariableNameMacro extends Macro {
  @Override
  public String getName() {
    return "dartSuggestVariableName";
  }

  @NotNull
  @Override
  public String getDefaultValue() {
    return "o";
  }

  @Override
  public Result calculateResult(Expression @NotNull [] params, ExpressionContext context) {
    return null;
  }
}
