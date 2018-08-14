package com.jetbrains.lang.dart.ide.surroundWith.expression;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartNamedArgument;
import org.jetbrains.annotations.NotNull;

public class DartWithBracketsExpressionSurrounder extends DartWithExpressionSurrounder {

  @Override
  public boolean isApplicable(@NotNull PsiElement[] elements) {
    // Limit this to named arguments; the intent is to convert a Flutter child: param to children:, which may involve creating red code.
    return super.isApplicable(elements) && elements[0].getParent() instanceof DartNamedArgument;
  }

  @Override
  public String getTemplateDescription() {
    return "[expr]";
  }

  @Override
  protected String getTemplateText(PsiElement expr) {
    return expr.textContains('\n')
           ? "[\n" + expr.getText() + ",]\n"
           : "[" + expr.getText() + "]";
  }
}
