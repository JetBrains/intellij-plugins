// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.contextInfo;

import com.intellij.codeInsight.hint.DeclarationRangeHandler;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartDeclarationRangeHandler implements DeclarationRangeHandler<DartPsiCompositeElement> {
  @Override
  @Nullable
  public TextRange getDeclarationRange(@NotNull DartPsiCompositeElement element) {
    // returned range is extended to full line(s) in platform
    if (element instanceof DartCallExpression) {
      return TextRange.create(element.getTextRange().getStartOffset(),
                              ((DartCallExpression)element).getExpression().getTextRange().getEndOffset());
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