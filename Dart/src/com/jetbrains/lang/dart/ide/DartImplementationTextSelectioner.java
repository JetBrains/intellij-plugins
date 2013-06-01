package com.jetbrains.lang.dart.ide;

import com.intellij.codeInsight.hint.ImplementationTextSelectioner;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartComponentName;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartImplementationTextSelectioner implements ImplementationTextSelectioner {
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
    final TextRange textRange = element.getTextRange();
    return textRange.getEndOffset();
  }
}
