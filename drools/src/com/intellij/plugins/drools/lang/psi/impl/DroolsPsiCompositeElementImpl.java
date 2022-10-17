// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.DroolsPsiCompositeElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class DroolsPsiCompositeElementImpl extends ASTWrapperPsiElement implements DroolsPsiCompositeElement {
  public DroolsPsiCompositeElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public IElementType getTokenType() {
    return getNode().getElementType();
  }

  @Override
  public String toString() {
    return getTokenType().toString();
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    return super.processDeclarations(processor, state, lastParent, place);
  }
}
