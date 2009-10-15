package com.intellij.tapestry.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: 09.10.2009
 *         Time: 16:20:36
 */
public interface TelExpression extends PsiElement {

  @Nullable
  PsiType getPsiType();
}
