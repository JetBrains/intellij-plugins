package com.intellij.tapestry.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public interface TelExpression extends PsiElement {

  @Nullable
  PsiType getPsiType();
}
