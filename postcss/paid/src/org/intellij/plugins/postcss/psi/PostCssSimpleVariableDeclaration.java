package org.intellij.plugins.postcss.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.css.CssNamedElement;
import com.intellij.psi.css.CssTermList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PostCssSimpleVariableDeclaration extends CssNamedElement, PsiNameIdentifierOwner {
  @NotNull
  @Override
  String getName();

  @NotNull
  @Override
  PsiElement getNameIdentifier();

  @Nullable
  CssTermList getInitializer();
}
