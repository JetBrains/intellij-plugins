package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeBlockImpl extends CompositePsiElement {
  public JadeBlockImpl() {
    super(JadeElementTypes.BLOCK);
  }
}
