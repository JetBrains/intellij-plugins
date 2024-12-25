// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.template.macro;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartClassNameMacro extends DartMacroBase {
  @Override
  public String getName() {
    return "dartClassName";
  }

  @Override
  public Result calculateResult(Expression @NotNull [] params, final ExpressionContext context) {
    final String containingClassName = getContainingClassName(context.getPsiElementAtStartOffset());
    return containingClassName == null ? null : new TextResult(containingClassName);
  }

  @VisibleForTesting
  public @Nullable String getContainingClassName(final @Nullable PsiElement element) {
    if (element == null) return null;
    final DartClass dartClass = PsiTreeUtil.getParentOfType(element, DartClass.class);
    return dartClass == null ? null : dartClass.getName();
  }
}
