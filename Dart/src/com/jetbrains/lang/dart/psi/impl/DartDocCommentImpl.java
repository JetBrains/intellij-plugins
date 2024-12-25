// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.psi.DartDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartDocCommentImpl extends ASTWrapperPsiElement implements DartDocComment {

  public DartDocCommentImpl(final @NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable PsiElement getOwner() {
    return null; // todo
  }

  @Override
  public @NotNull IElementType getTokenType() {
    return getNode().getElementType();
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this);
  }
}
