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

public class TestUtil {

  @Nullable
  public static String findGroupOrTestName(@Nullable final DartCallExpression expression) {
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

  @Nullable
  public static PsiElement findTestElement(@Nullable PsiElement element) {
    DartCallExpression callExpression = PsiTreeUtil.getParentOfType(element, DartCallExpression.class, false);
    while (callExpression != null) {
      if (isGroup(callExpression) || isTest(callExpression)) {
        return callExpression;
      }
      callExpression = PsiTreeUtil.getParentOfType(callExpression, DartCallExpression.class, true);
    }
    return element != null ? element.getContainingFile() : null;
  }

  public static boolean isTest(@NotNull final DartCallExpression expression) {
    return checkCalledFunctionName(expression, "test");
  }

  public static boolean isGroup(@NotNull final DartCallExpression expression) {
    return checkCalledFunctionName(expression, "group");
  }

  private static boolean checkCalledFunctionName(@NotNull final DartCallExpression callExpression, @NotNull final String expectedName) {
    return expectedName.equals(callExpression.getExpression().getText());
  }
}
