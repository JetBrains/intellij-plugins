package org.intellij.plugins.postcss.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.css.CssNamedElement;
import org.jetbrains.annotations.NotNull;

public interface PostCssSimpleVariableDeclaration extends CssNamedElement, PsiNameIdentifierOwner {
  @NotNull
  @Override
  String getName();

  @NotNull
  @Override
  PsiElement getNameIdentifier();
}
