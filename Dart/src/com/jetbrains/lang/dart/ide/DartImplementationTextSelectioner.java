// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide;

import com.intellij.codeInsight.hint.ImplementationTextSelectioner;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartVarAccessDeclaration;
import com.jetbrains.lang.dart.psi.DartVarInit;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public final class DartImplementationTextSelectioner implements ImplementationTextSelectioner {
  @Override
  public int getTextStartOffset(@NotNull PsiElement element) {
    if (element instanceof DartComponentName) {
      element = element.getParent();
    }
    final TextRange textRange = element.getTextRange();
    return textRange.getStartOffset();
  }

  @Override
  public int getTextEndOffset(@NotNull PsiElement element) {
    if (element instanceof DartComponentName) {
      element = element.getParent();
    }
    if (element instanceof DartVarAccessDeclaration) {
      PsiElement nextSibling = UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(element);
      if (nextSibling instanceof DartVarInit) {
        element = nextSibling;
      }
    }
    final TextRange textRange = element.getTextRange();
    return textRange.getEndOffset();
  }
}
