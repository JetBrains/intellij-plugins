package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeConditionalHeaderImpl extends CompositePsiElement {
  public JadeConditionalHeaderImpl() {
    super(JadeElementTypes.CONDITIONAL_HEADER);
  }
}
