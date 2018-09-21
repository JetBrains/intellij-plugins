// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlPsiUtil;
import com.intellij.coldFusion.model.psi.CfmlScopeProvider;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlAttributeNameImpl extends CfmlAttributeImpl {
  public CfmlAttributeNameImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getAttributeName() {
    return "name";
  }

  @NotNull
  @Override
  public PsiElement getNavigationElement() {
    PsiElement element = findChildByType(CfmlElementTypes.ATTRIBUTE_VALUE);
    return element != null ? element : this;
  }

  @Override
  public int getProvidedScope() {
    final PsiElement parent = getParent();
    if (parent instanceof CfmlScopeProvider) {
      return ((CfmlScopeProvider)parent).getProvidedScope();
    }
    return super.getProvidedScope();
  }

  @Override
  public int getTextOffset() {
    PsiElement element = getNavigationElement();
    return element.getTextRange().getStartOffset();
  }

  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
    CheckUtil.checkWritable(this);
    final PsiElement newElement = CfmlPsiUtil.createConstantString(name, getProject());
    //noinspection ConstantConditions
    getNode().replaceChild(getValueElement().getNode(), newElement.getNode());
    return this;
  }

  @Override
  @NotNull
  public String getName() {
    final String name = getAttributeName();
    return name != null ? name : "";
  }
}
