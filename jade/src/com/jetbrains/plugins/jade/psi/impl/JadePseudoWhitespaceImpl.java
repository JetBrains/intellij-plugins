package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadePseudoWhitespaceImpl extends CompositePsiElement implements PsiWhiteSpace {
  public JadePseudoWhitespaceImpl() {
    super(JadeElementTypes.JADE_PSEUDO_WHITESPACE);
  }
}
