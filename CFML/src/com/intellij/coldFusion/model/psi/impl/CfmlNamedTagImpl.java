// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 */
public class CfmlNamedTagImpl extends CfmlTagImpl implements PsiNameIdentifierOwner {
  public CfmlNamedTagImpl(ASTNode astNode) {
    super(astNode);
  }

  @NotNull
  @Override
  public String getName() {
    CfmlAttributeNameImpl attribute = findChildByClass(CfmlAttributeNameImpl.class);
    if (attribute == null) {
      return "";
    }
    final String value = attribute.getPureAttributeValue();
    return value != null ? value : "";
  }

  @NotNull
  @Override
  public PsiElement getNavigationElement() {
    PsiElement namedAttribute = findChildByClass(CfmlAttributeNameImpl.class);
    return namedAttribute != null ? namedAttribute.getNavigationElement() : this;
  }

  @Override
  public int getTextOffset() {
    if (getNavigationElement() == this) {
      return super.getTextOffset();
    }
    return getNavigationElement().getTextOffset();
  }

  @Override
  public PsiElement getNameIdentifier() {
    return getNavigationElement();
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
}
