// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.template.macro;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import org.jetbrains.annotations.NotNull;

public final class DartSuggestVariableNameMacro extends DartMacroBase {
  @Override
  public String getName() {
    return "dartSuggestVariableName";
  }

  @Override
  public @NotNull String getDefaultValue() {
    return "o";
  }

  @Override
  public Result calculateResult(Expression @NotNull [] params, ExpressionContext context) {
    return null;
  }
}
