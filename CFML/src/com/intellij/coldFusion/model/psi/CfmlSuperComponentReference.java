// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlSuperComponentReference extends CfmlCompositeElement implements CfmlReference {
  public CfmlSuperComponentReference(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public final PsiReference getReference() {
    return this;
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    PsiElement resolveResult = resolve();
    if (resolveResult == null) {
      return ResolveResult.EMPTY_ARRAY;
    }
    return new ResolveResult[]{new PsiElementResolveResult(resolveResult, false)};
  }

  @Override
  public @NotNull PsiElement getElement() {
    return this;
  }


  @Override
  public @NotNull TextRange getRangeInElement() {
    return new TextRange(0, getTextLength());
  }

  @Override
  public PsiElement resolve() {
    CfmlComponent componentDefinition = getComponentDefinition();
    if (componentDefinition != null) {
      CfmlComponent aSuper = componentDefinition.getSuper();
      if (aSuper != null) {
        return aSuper;
      }
    }
    return null;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    throw new IncorrectOperationException("Can't rename a keyword");
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    throw new IncorrectOperationException("Can't bind a keyword");
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    CfmlComponent componentDefinition = getComponentDefinition();
    if (componentDefinition != null) {
      CfmlComponentReference superReference = componentDefinition.getSuperReference();
      if (superReference == null) {
        return false;
      }
      return superReference.isReferenceTo(element);
    }
    return false;
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  private @Nullable CfmlComponent getComponentDefinition() {
    return getContainingFile().getComponentDefinition();
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }
}
