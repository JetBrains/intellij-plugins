package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeIncludeStatementImpl extends CompositePsiElement {
  public JadeIncludeStatementImpl() {
    super(JadeElementTypes.INCLUDE_STATEMENT);
  }
}
