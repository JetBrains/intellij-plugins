package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeClassImpl extends CompositePsiElement {

  public JadeClassImpl() {
    super(JadeElementTypes.CLASS);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + getElementType().toString() + ")";
  }

  //@Override
  //public PsiReference getReference() {
  //  return new HtmlCssClassOrIdReference(this, 1, getTextLength(), false, true);
  //}
}
