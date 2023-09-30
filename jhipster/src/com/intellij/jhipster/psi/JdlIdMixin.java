// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.jhipster.psi.impl.JdlValueImpl;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

public abstract class JdlIdMixin extends JdlValueImpl implements JdlId {
  public JdlIdMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiReference getReference() {
    PsiElement parent = getParent();
    if (parent instanceof JdlEntitiesList
        || parent instanceof JdlRelationshipEntity) {
      return new JdlEntityIdReference(this);
    }
    if (parent instanceof JdlFieldConstraintParameters) {
      return new JdlConstantNameReference(this);
    }
    return null;
  }
}
