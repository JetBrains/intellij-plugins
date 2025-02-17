package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class JadeConditionalElseImpl extends CompositePsiElement {
  public JadeConditionalElseImpl(@NotNull IElementType type) {
    super(type);
  }
}
