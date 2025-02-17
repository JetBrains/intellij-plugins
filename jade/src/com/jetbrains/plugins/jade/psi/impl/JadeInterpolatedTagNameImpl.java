package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeInterpolatedTagNameImpl extends CompositePsiElement {
  public JadeInterpolatedTagNameImpl() {
    super(JadeElementTypes.TAG_INTERP_NAME);
  }
}
