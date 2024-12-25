// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.psi.CfmlParameter;
import com.intellij.coldFusion.model.psi.CfmlPsiUtil;
import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlTagFunctionParameterImpl extends CfmlTagImpl implements CfmlParameter, CfmlVariable {
  public static final String TAG_NAME = "cfargument";

  public CfmlTagFunctionParameterImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    CheckUtil.checkWritable(this);
    CfmlAttributeNameImpl childByClass = findChildByClass(CfmlAttributeNameImpl.class);
    if (childByClass != null) {
      childByClass.setName(name);
    }
    return this;
  }

  @Override
  public @NotNull String getName() {
    final CfmlAttributeNameImpl attribute = findChildByClass(CfmlAttributeNameImpl.class);
    if (attribute != null) {
      return attribute.getName();
    }
    return "";
  }

  @Override
  public boolean isRequired() {
    String requiredAttr = CfmlPsiUtil.getPureAttributeValue(this, "required");
    if (requiredAttr == null) {
      return false;
    }
    requiredAttr = StringUtil.toLowerCase(requiredAttr);
    return "yes".equals(requiredAttr) || "true".equals(requiredAttr);
  }

  @Override
  public String getType() {
    return CfmlPsiUtil.getPureAttributeValue(this, "type");
  }

  @Override
  public int getProvidedScope() {
    return CfmlScopesInfo.ARGUMENTS_SCOPE;
  }

  @Override
  public @NotNull String getTagName() {
    return TAG_NAME;
  }

  @Override
  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  @Override
  public @NotNull PsiElement getNavigationElement() {
    final CfmlAttributeNameImpl parameterName = findChildByClass(CfmlAttributeNameImpl.class);
    if (parameterName != null) {
      return parameterName.getNavigationElement();
    }
    return super.getNavigationElement();
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    return processor.execute(this, state);
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  public @NotNull String getlookUpString() {
    return getName();
  }

  @Override
  public int getTextOffset() {
    if (getNavigationElement() == this) {
      return super.getTextOffset();
    }
    return getNavigationElement().getTextOffset();
  }
}
