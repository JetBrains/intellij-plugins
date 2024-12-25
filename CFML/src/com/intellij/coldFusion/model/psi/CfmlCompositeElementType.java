// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlCompositeElementType extends IElementType implements ICompositeElementType {
  public CfmlCompositeElementType(final @NotNull @NonNls String debugName) {
    super(debugName, CfmlLanguage.INSTANCE);
  }

  public PsiElement createPsiElement(ASTNode node) {
    return new CfmlCompositeElement(node);
  }

  @Override
  public @NotNull ASTNode createCompositeNode() {
    return new CompositeElement(this);
  }
}
