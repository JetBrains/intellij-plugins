// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.CfmlProperty;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlTagPropertyImpl extends CfmlNamedTagImpl implements CfmlProperty {
  public CfmlTagPropertyImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  public PsiElement getNameIdentifier() {
    return getAttributeValueElement("name");
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }

  @Override
  public @Nullable CfmlComponent getComponent() {
    return PsiTreeUtil.findChildOfType(this, CfmlComponent.class);
  }

  private boolean checkBooleanAttribute(String attributeName) {
    PsiElement attributeValue = getAttributeValueElement(attributeName);
    if (attributeValue != null) {
      if ("true".equalsIgnoreCase(attributeValue.getText())) {
        return true;
      }
      return false;
    }

    CfmlComponent component = getComponent();
    return component != null ? component.hasImplicitAccessors() : false;
  }

  @Override
  public boolean hasGetter() {
    return checkBooleanAttribute("getter");
  }

  @Override
  public boolean hasSetter() {
    return checkBooleanAttribute("setter");
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }

  @Override
  public @NotNull String getlookUpString() {
    return getName();
  }
}
