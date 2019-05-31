/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package com.jetbrains.lang.dart.projectView;

import com.intellij.codeInsight.hint.DeclarationRangeHandler;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartClassDefinition;
import com.jetbrains.lang.dart.psi.DartFunctionExpression;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import org.jetbrains.annotations.NotNull;

public class DartDeclarationRangeHandler implements DeclarationRangeHandler {
  @Override
  public @NotNull
  TextRange getDeclarationRange(@NotNull PsiElement component) {
    // Find the named parent of the given component -- either a dart call expression or a class definition.
    PsiElement namedParent = component;
    while (!(namedParent == null ||
             namedParent instanceof DartClassDefinition ||
             namedParent instanceof DartFunctionExpression ||
             namedParent instanceof DartMethodDeclaration)) {
      namedParent = namedParent.getParent();
    }
    // We found nothing, so default to the first line of the given component.
    if (namedParent == null) {
      namedParent = component;
    }
    final int offset = namedParent.getTextOffset();
    final int firstLineLength = namedParent.getText().indexOf('\n');
    if (firstLineLength == -1) return namedParent.getTextRange();
    return new TextRange(offset, offset + firstLineLength);
  }
}
