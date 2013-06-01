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

/**
 * @author: Fedor.Korotkov
 */
public abstract class DartWithExpressionSurrounder implements Surrounder {
  public boolean isApplicable(@NotNull PsiElement[] elements) {
    return elements.length == 1 && elements[0] instanceof DartExpression;
  }

  @Nullable
  protected DartExpression getSurroundedNode(@NotNull final PsiElement element) {
    return DartElementGenerator.createExpressionFromText(
      element.getProject(),
      getTemplateText(element)
    );
  }

  @Nullable
  public TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement[] elements)
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
