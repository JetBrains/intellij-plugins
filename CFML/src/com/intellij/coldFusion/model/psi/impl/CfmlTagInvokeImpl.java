// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

// corresponding text pattern: <cfinvoke ... >
public class CfmlTagInvokeImpl extends CfmlTagImpl implements CfmlFunctionCall {
  private static final String TAG_NAME = "cfinvoke";

  public CfmlTagInvokeImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }

  @Override
  public CfmlReference getReferenceExpression() {
    return findNotNullChildByClass(CfmlReferenceExpression.class);
  }

  @Override
  public @NotNull PsiElement getNavigationElement() {
    return getReferenceExpression();
  }

  @Override
  public CfmlArgumentList findArgumentList() {
    return null;
  }

  @Override
  public PsiType[] getArgumentTypes() {
    return PsiType.EMPTY_ARRAY;
  }

  @Override
  public String getName() {
    CfmlReferenceExpression name = findChildByClass(CfmlReferenceExpression.class);
    return name != null ? name.getName() : null;
  }

  @Override
  public @NotNull String getTagName() {
    return TAG_NAME;
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    final PsiElement reference = getAttributeValueElement("component");

    if (reference != null) {
      ASTNode referenceNode = reference.getNode();
      if (referenceNode != null) {
        return new PsiReference[]{new CfmlComponentReference(referenceNode, this)};
      }
    }
    return super.getReferences();
  }
}
