package com.jetbrains.lang.dart.ide.surroundWith.expression;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartBundle;

/**
 * @author: Fedor.Korotkov
 */
public class DartWithNotParenthesisExpressionSurrounder extends DartWithExpressionSurrounder {
  @Override
  protected String getTemplateText(PsiElement expr) {
    return "!(" + expr.getText() + ")";
  }

  @Override
  public String getTemplateDescription() {
    return DartBundle.message("dart.surround.with.not.parenthesis");
  }
}
