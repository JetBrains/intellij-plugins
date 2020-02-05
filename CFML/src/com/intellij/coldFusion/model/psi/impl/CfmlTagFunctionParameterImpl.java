// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.psi.*;
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
import org.jetbrains.annotations.Nullable;

public class CfmlTagFunctionParameterImpl extends CfmlTagImpl implements CfmlParameter, CfmlTypedVariable {
  public final static String TAG_NAME = "cfargument";

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

  @NotNull
  @Override
  public String getName() {
    final CfmlAttributeNameImpl attribute = findChildByClass(CfmlAttributeNameImpl.class);
    if (attribute != null) {
      return attribute.getName();
    }
    return "";
  }

  @Nullable
  @Override
  public String getDescription() {
    return CfmlPsiUtil.getPureAttributeValue(this, "hint");
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
  public String getDefault() {
    return CfmlPsiUtil.getPureAttributeValue(this, "default");
  }

  @Override
  public int getProvidedScope() {
    return CfmlScopesInfo.ARGUMENTS_SCOPE;
  }

  @Override
  @NotNull
  public String getTagName() {
    return TAG_NAME;
  }

  @Override
  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  @NotNull
  @Override
  public PsiElement getNavigationElement() {
    final PsiElement parameterName = findChildByClass(CfmlAttributeNameImpl.class);
    if (parameterName != null) {
      PsiElement navigationElement = parameterName.getNavigationElement();
      if (navigationElement != null) {
        return navigationElement;
      }
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
  public boolean isTrulyDeclaration() {
    return true;
  }

  @NotNull
  @Override
  public String getlookUpString() {
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
