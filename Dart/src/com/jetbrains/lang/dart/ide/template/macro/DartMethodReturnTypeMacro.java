// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.template.macro;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartMethodReturnTypeMacro extends DartMacroBase {
  @Override
  public String getName() {
    return "dartReturnType";
  }

  @Override
  public @Nullable Result calculateResult(final Expression @NotNull [] params, final ExpressionContext context) {
    final String returnType = getContainingFunctionReturnType(context.getPsiElementAtStartOffset());
    return returnType == null ? null : new TextResult(returnType);
  }

  @VisibleForTesting
  public @Nullable String getContainingFunctionReturnType(final @Nullable PsiElement element) {
    if (element == null) return null;
    final DartComponent parent = PsiTreeUtil.getParentOfType(element,
                                                             DartGetterDeclaration.class,
                                                             DartFunctionDeclarationWithBodyOrNative.class,
                                                             DartMethodDeclaration.class,
                                                             DartFunctionDeclarationWithBody.class);

    if (parent instanceof DartGetterDeclaration) {
      return getReturnTypeString(((DartGetterDeclaration)parent).getReturnType());
    }
    if (parent instanceof DartMethodDeclaration) {
      return getReturnTypeString(((DartMethodDeclaration)parent).getReturnType());
    }
    if (parent instanceof DartFunctionDeclarationWithBodyOrNative) {
      return getReturnTypeString(((DartFunctionDeclarationWithBodyOrNative)parent).getReturnType());
    }
    if (parent instanceof DartFunctionDeclarationWithBody) {
      return getReturnTypeString(((DartFunctionDeclarationWithBody)parent).getReturnType());
    }

    return null;
  }

  private static String getReturnTypeString(final @Nullable DartReturnType returnType) {
    return returnType == null ? null : returnType.getText();
  }
}
