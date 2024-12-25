// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.editor;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.DartExpression;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartSelectionFilter implements Condition<PsiElement> {
  @Override
  public boolean value(final @NotNull PsiElement psiElement) {
    if (getSiblingSemicolonIfExpression(psiElement) != null) {
      return false; // DartWordSelectionHandler will select this expression with the following semicolon
    }
    return true;
  }

  public static @Nullable PsiElement getSiblingSemicolonIfExpression(final @NotNull PsiElement psiElement) {
    if ((psiElement instanceof DartExpression)) {
      final PsiElement last = PsiTreeUtil.getDeepestLast(psiElement);
      if (last.getNode().getElementType() != DartTokenTypes.SEMICOLON) {
        final PsiElement next = UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(psiElement);
        if (next != null && next.getNode().getElementType() == DartTokenTypes.SEMICOLON) {
          return next;
        }
      }
    }
    return null;
  }
}
