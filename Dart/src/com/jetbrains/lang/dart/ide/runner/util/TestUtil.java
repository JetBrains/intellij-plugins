package com.jetbrains.lang.dart.ide.runner.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartArgumentList;
import com.jetbrains.lang.dart.psi.DartArguments;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestUtil {

  @Nullable
  public static String findTestName(@Nullable DartCallExpression expression) {
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

  public static String suggestedName(String path, Scope scope, String testName) {
    if (path != null) {
      final String fileName = PathUtil.getFileName(path);
      switch (scope) {
        case METHOD:
          return DartBundle.message("test.0.in.1", testName, fileName);
        case GROUP:
          return DartBundle.message("test.group.0.in.1", testName, fileName);
        case FILE:
          return DartBundle.message("all.tests.in.0", fileName);
        case FOLDER:
          return DartBundle.message("all.tests.in.0", PathUtil.getFileName(path));
      }
    }
    return null;
  }
}
