// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.UI.CfmlLookUpItemUtil;
import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.ui.IconManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CfmlFunctionImpl extends CfmlCompositeElement implements CfmlFunction, PsiNameIdentifierOwner {
  public CfmlFunctionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
    CheckUtil.checkWritable(this);
    final PsiElement newElement = CfmlPsiUtil.createReferenceExpression(name, getProject());
    //noinspection ConstantConditions
    getNode().replaceChild(getReferenceElement().getNode(), newElement.getNode());
    return this;
  }

  @Nullable
  public PsiElement getReferenceElement() {
    return findChildByType(CfscriptTokenTypes.IDENTIFIER);
  }

  @NotNull
  public String getFunctionName() {
    PsiElement element = getReferenceElement();
    return element != null ? element.getText() : "";
  }

  @NotNull
  @Override
  public String getName() {
    return getFunctionName();
  }

  @NotNull
  @Override
  public PsiElement getNavigationElement() {
    PsiElement element = getReferenceElement();
    return element != null ? element : this;
  }

  @Override
  @NotNull
  public String getParametersAsString() {
    return getFunctionInfo().getParametersListPresentableText();
  }

  @Override
  public CfmlParameter @NotNull [] getParameters() {
    final CfmlParametersList parametersList = findChildByClass(CfmlParametersList.class);
    if (parametersList != null) {
      return parametersList.getParameters();
    }
    return CfmlFunctionParameterImpl.EMPTY_ARRAY;
  }

  @Nullable
  public CfmlParametersList getParametersList() {
    return findChildByClass(CfmlParametersList.class);
  }

  @Override
  @Nullable
  public PsiType getReturnType() {
    final PsiElement type = findChildByType(CfmlElementTypes.TYPE);
    return type != null ?
           new CfmlComponentType(type.getText(), getContainingFile(), getProject()) : null;
  }

  @Override
  public Icon getIcon(int flags) {
    return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    processor.execute(this, state);
    final CfmlParametersList params = findChildByClass(CfmlParametersList.class);
    if (params != null) {
      if (!params.processDeclarations(processor, state, null, params)) {
        return false;
      }
    }
    // FUNCTIONBODY element contains all other declarations
    return true;
  }

  @Override
  public PsiElement getNameIdentifier() {
    return getReferenceElement();
  }

  @Override
  public int getTextOffset() {
    final PsiElement element = getNavigationElement();
    return element.getTextRange().getStartOffset();
  }

  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  @NotNull
  public CfmlFunctionDescription getFunctionInfo() {
    return CfmlLookUpItemUtil.getFunctionDescription(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CfmlRecursiveElementVisitor) {
      ((CfmlRecursiveElementVisitor)visitor).visitCfmlFunction(this);
    }
    else {
      super.accept(visitor);
    }
  }
}
