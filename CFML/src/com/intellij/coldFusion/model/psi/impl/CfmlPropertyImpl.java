// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.coldFusion.model.psi.CfmlProperty;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlPropertyImpl extends CfmlCompositeElement implements CfmlProperty {
  public CfmlPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  public String getName() {
    PsiElement nameIdentifier = getNameIdentifier();
    return nameIdentifier != null ? nameIdentifier.getText() : "";
  }

  @Override
  public PsiElement getNameIdentifier() {
    PsiElement lastChild = getLastChild();
    if (lastChild == null) {
      return null;
    }
    ASTNode node = lastChild.getNode();
    if (node != null && node.getElementType() == CfscriptTokenTypes.IDENTIFIER) {
      return lastChild;
    }
    else {
      CfmlNamedAttributeImpl namedAttribute = PsiTreeUtil.findChildOfType(this, CfmlNamedAttributeImpl.class);
      if (namedAttribute != null) {
        return namedAttribute.getNameIdentifier();
      }
    }
    return null;
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }

  @Override
  public boolean hasGetter() {
    return false;
  }

  @Override
  public boolean hasSetter() {
    return false;
  }

  @Override
  public CfmlComponent getComponent() {
    return PsiTreeUtil.getParentOfType(this, CfmlComponent.class);
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }

  @Override
  public @NotNull String getlookUpString() {
    String name = getName();
    return name != null ? name : "";
  }
}
