package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeCaseStatementImpl extends CompositePsiElement {
  public JadeCaseStatementImpl() {
    super(JadeElementTypes.CASE_STATEMENT);
  }
}
