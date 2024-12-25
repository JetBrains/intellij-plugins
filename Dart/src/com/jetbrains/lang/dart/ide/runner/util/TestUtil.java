// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartArgumentList;
import com.jetbrains.lang.dart.psi.DartArguments;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TestUtil {

  public static @Nullable String findGroupOrTestName(final @Nullable DartCallExpression expression) {
    String testName;
    final DartArguments arguments = expression == null ? null : expression.getArguments();
    final DartArgumentList dartArgumentList = arguments == null ? null : arguments.getArgumentList();
    if (dartArgumentList == null || dartArgumentList.getExpressionList().isEmpty()) {
      return null;
    }
    final DartExpression dartExpression = dartArgumentList.getExpressionList().get(0);
    testName = dartExpression == null ? "" : StringUtil.unquoteString(dartExpression.getText());
    return testName;
  }

  public static @Nullable PsiElement findTestElement(@Nullable PsiElement element) {
    DartCallExpression callExpression = PsiTreeUtil.getParentOfType(element, DartCallExpression.class, false);
    while (callExpression != null) {
      if (isGroup(callExpression) || isTest(callExpression)) {
        return callExpression;
      }
      callExpression = PsiTreeUtil.getParentOfType(callExpression, DartCallExpression.class, true);
    }
    return element != null ? element.getContainingFile() : null;
  }

  public static boolean isTest(final @NotNull DartCallExpression expression) {
    return checkCalledFunctionName(expression, "test");
  }

  public static boolean isGroup(final @NotNull DartCallExpression expression) {
    return checkCalledFunctionName(expression, "group");
  }

  private static boolean checkCalledFunctionName(final @NotNull DartCallExpression callExpression, final @NotNull String expectedName) {
    DartExpression expression = callExpression.getExpression();
    return expression != null && expectedName.equals(expression.getText());
  }
}
