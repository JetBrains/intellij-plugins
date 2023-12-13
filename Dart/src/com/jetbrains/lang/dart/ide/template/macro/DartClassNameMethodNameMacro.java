package com.jetbrains.lang.dart.ide.template.macro;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import org.jetbrains.annotations.NotNull;

public final class DartClassNameMethodNameMacro extends DartMacroBase {
  @Override
  public String getName() {
    return "dartClassNameMethodName";
  }

  @Override
  public Result calculateResult(Expression @NotNull [] params, final ExpressionContext context) {
    final Result classNameResult = (new DartClassNameMacro()).calculateResult(params, context);
    final Result methodNameResult = (new DartMethodNameMacro()).calculateResult(params, context);

    if (classNameResult != null && methodNameResult != null) {
      return new TextResult(classNameResult.toString() + "." + methodNameResult.toString());
    }
    else if (classNameResult == null && methodNameResult != null) {
      return new TextResult(methodNameResult.toString());
    }
    else if (classNameResult != null) {
      return new TextResult(classNameResult.toString());
    }
    else {
      return null;
    }
  }
}

