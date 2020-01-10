// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlComponentType;
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
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlTagParamImpl extends CfmlTagImpl implements CfmlVariable {
  public final static String TAG_NAME = "cfparam";

  public CfmlTagParamImpl(ASTNode astNode) {
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
  
  private String[] getScopeNameSplit() {
    final CfmlAttributeNameImpl attribute = findChildByClass(CfmlAttributeNameImpl.class);
    if (attribute == null) {
      return new String[]{null, ""};
    }
    String name = attribute.getName();
    String[] split = name.split("\\.", 1);
    if (split.length > 1 && ArrayUtil.find(CfmlUtil.getVariableScopes(getProject()), split[0]) > -1) {
      return split;
    }
    return new String[]{null, name};
  }
  
  @NotNull
  @Override
  public String getName() {
    return getScopeNameSplit()[1];
  }

  @Nullable
  public String getDescription() {
    return CfmlPsiUtil.getPureAttributeValue(this, "hint");
  }

  public boolean isRequired() {
    String requiredAttr = CfmlPsiUtil.getPureAttributeValue(this, "required");
    if (requiredAttr == null) {
      return false;
    }
    requiredAttr = StringUtil.toLowerCase(requiredAttr);
    return "yes".equals(requiredAttr) || "true".equals(requiredAttr);
  }

  public String getType() {
    return CfmlPsiUtil.getPureAttributeValue(this, "type");
  }

  @Override
  public int getProvidedScope() {
    return CfmlScopesInfo.getScopeByString(getScopeNameSplit()[0]);
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
  public PsiType getPsiType() {
    final String returnTypeString = this.getType();
    return returnTypeString != null ?
           new CfmlComponentType(returnTypeString, getContainingFile(), getProject()) : null;
  }

  @Override
  public boolean isTrulyDeclaration() {
    return false;
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
