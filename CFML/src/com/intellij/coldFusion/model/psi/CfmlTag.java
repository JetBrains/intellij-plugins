package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiElement;

/**
 * @author vnikolaenko
 */
public interface CfmlTag extends PsiElement {
  String getTagName();

  PsiElement getDeclarativeElement();
}
