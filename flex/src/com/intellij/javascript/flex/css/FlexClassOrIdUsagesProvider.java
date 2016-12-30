package com.intellij.javascript.flex.css;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssTerm;
import com.intellij.psi.css.impl.CssTermTypes;
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider;
import org.jetbrains.annotations.NotNull;

public class FlexClassOrIdUsagesProvider extends CssClassOrIdReferenceBasedUsagesProvider {
  @Override
  protected boolean acceptElement(@NotNull PsiElement candidate) {
    return (candidate instanceof CssTerm && ((CssTerm)candidate).getTermType() == CssTermTypes.IDENT) ||
           candidate instanceof JSLiteralExpression;
  }
}
