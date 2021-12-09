// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.surroundWith.expression;

import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.psi.DartExpression;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartWithExpressionSurrounder implements Surrounder {
  @Override
  public boolean isApplicable(PsiElement @NotNull [] elements) {
    return elements.length == 1 && elements[0] instanceof DartExpression;
  }

  @Nullable
  protected DartExpression getSurroundedNode(@NotNull final PsiElement element) {
    return DartElementGenerator.createExpressionFromText(
      element.getProject(),
      getTemplateText(element)
    );
  }

  @Override
  @Nullable
  public TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, PsiElement @NotNull [] elements)
    throws IncorrectOperationException {
    PsiElement source = elements[0];

    final DartExpression parenthExprNode = getSurroundedNode(source);
    if (parenthExprNode == null) {
      throw new IncorrectOperationException("Can't create expression for: " + source.getText());
    }

    final PsiElement replace = source.replace(parenthExprNode);
    final int endOffset = replace.getTextRange().getEndOffset();
    return TextRange.create(endOffset, endOffset);
  }

  protected abstract String getTemplateText(PsiElement expr);
}
