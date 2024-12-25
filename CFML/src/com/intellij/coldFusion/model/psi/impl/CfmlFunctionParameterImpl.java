// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.coldFusion.model.psi.CfmlParameter;
import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlFunctionParameterImpl extends CfmlCompositeElement implements CfmlParameter, CfmlVariable {
  public static final CfmlFunctionParameterImpl[] EMPTY_ARRAY = new CfmlFunctionParameterImpl[0];

  public CfmlFunctionParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }

  @Override
  public @NotNull String getName() {
    final PsiElement parameterName = findChildByType(CfscriptTokenTypes.IDENTIFIER);
    if (parameterName != null) {
      return parameterName.getText();
    }
    return "";
  }

  @Override
  public boolean isRequired() {
    final PsiElement element = findChildByType(CfscriptTokenTypes.REQUIRED_KEYWORD);
    return element != null;
  }

  @Override
  public String getType() {
    final PsiElement typeElement = findChildByType(CfmlElementTypes.TYPE);
    if (typeElement != null) {
      return typeElement.getText();
    }
    return null;
  }

  @Override
  public int getProvidedScope() {
    return CfmlScopesInfo.ARGUMENTS_SCOPE;
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  @Override
  public @NotNull PsiElement getNavigationElement() {
    final PsiElement parameterName = findChildByType(CfscriptTokenTypes.IDENTIFIER);
    return parameterName != null ? parameterName : super.getNavigationElement();
  }

  @Override
  public @NotNull String getlookUpString() {
    return getName();
  }
}
