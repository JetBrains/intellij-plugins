package org.intellij.plugins.postcss.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.css.CssNamedElement;
import org.jetbrains.annotations.NotNull;

public interface PostCssCustomMedia extends CssNamedElement, PsiNameIdentifierOwner {
  @NotNull
  @Override
  PsiElement getNameIdentifier();
}