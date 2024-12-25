// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.contextInfo;

import com.intellij.codeInsight.hint.DeclarationRangeHandler;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartDeclarationRangeHandler implements DeclarationRangeHandler<DartPsiCompositeElement> {
  @Override
  public @Nullable TextRange getDeclarationRange(@NotNull DartPsiCompositeElement element) {
    // returned range is extended to full line(s) in platform
    if (element instanceof DartCallExpression) {
      DartExpression expression = ((DartCallExpression)element).getExpression();
      if (expression != null) {
        return TextRange.create(element.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset());
      }
    }
    if (element instanceof DartNewExpression) {
      DartReferenceExpression lastExpression = ContainerUtil.getLastItem(((DartNewExpression)element).getReferenceExpressionList());
      if (lastExpression != null) {
        return TextRange.create(element.getTextRange().getStartOffset(), lastExpression.getTextRange().getEndOffset());
      }
    }

    if (element instanceof DartComponent) {
      DartComponentName nameElement = ((DartComponent)element).getComponentName();
      if (nameElement != null) {
        return TextRange.create(element.getTextRange().getStartOffset(), nameElement.getTextRange().getEndOffset());
      }
    }

    return null;
  }
}