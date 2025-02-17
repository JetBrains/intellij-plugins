// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.PsiComment;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import org.jetbrains.annotations.NotNull;

public class JadeCommentImpl extends CompositePsiElement implements PsiComment {

  public JadeCommentImpl() {
    super(JadeElementTypes.COMMENT);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + getElementType().toString() + ")";
  }

  @Override
  public @NotNull IElementType getTokenType() {
    return getElementType();
  }
}
