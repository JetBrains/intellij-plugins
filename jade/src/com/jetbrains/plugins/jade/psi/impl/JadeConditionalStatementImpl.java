package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeConditionalStatementImpl extends CompositePsiElement {
  public JadeConditionalStatementImpl() {
    super(JadeElementTypes.CONDITIONAL_STATEMENT);
  }
}
