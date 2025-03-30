// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class JdlFieldTypeMixin extends ASTWrapperPsiElement implements JdlFieldType {
  public JdlFieldTypeMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable PsiReference getReference() {
    JdlFieldTypeReferenceProvider provider = JdlFieldTypeReferenceProvider.getInstance();
    return provider != null ? provider.createReference(this) : null;
  }
}
