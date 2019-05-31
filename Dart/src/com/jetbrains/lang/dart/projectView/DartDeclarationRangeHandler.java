/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package com.jetbrains.lang.dart.projectView;

import com.intellij.codeInsight.hint.DeclarationRangeHandler;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * A handler to compute the declaring {@link TextRange} of some {@link DartPsiCompositeElement}.
 */
public class DartDeclarationRangeHandler implements DeclarationRangeHandler<DartPsiCompositeElement> {
  @Override
  public @NotNull
  TextRange getDeclarationRange(@NotNull DartPsiCompositeElement component) {
    // Find the named parent of the given component -- either a dart call expression or a class definition.
    PsiElement namedParent = component;
    while (!(namedParent == null ||
             namedParent instanceof DartClassDefinition ||
             namedParent instanceof DartFunctionExpression ||
             namedParent instanceof DartMethodDeclaration)) {
      namedParent = namedParent.getParent();
    }
    // If we did not find a named parent of this component, default to the first line of the starting component.
    if (namedParent == null) {
      return getFirstLine(component);
    }
    return getFirstLine(namedParent);
  }

  /**
   * @return The first line of {@param element}'s {@link TextRange}, or the entire {@link TextRange} if it contains no new lines.
   */
  private TextRange getFirstLine(PsiElement element) {
    final int offset = element.getTextOffset();
    final int firstLineLength = element.getText().indexOf('\n');
    if (firstLineLength == -1) {
      return element.getTextRange();
    }
    return new TextRange(offset, offset + firstLineLength);
  }
}
