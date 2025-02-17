package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeYieldStatementImpl extends CompositePsiElement {
  public JadeYieldStatementImpl() {
    super(JadeElementTypes.YIELD_STATEMENT);
  }
}
