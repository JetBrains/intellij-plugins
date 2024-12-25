// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlAttribute;
import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlAttributeImpl extends CfmlCompositeElement implements CfmlAttribute {
  public CfmlAttributeImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    return getAttributeName();
  }

  public @Nullable PsiElement getValueElement() {
    return findChildByType(CfmlElementTypes.ATTRIBUTE_VALUE);
  }

  @Override
  public String getAttributeName() {
    PsiElement id = findChildByType(CfmlTokenTypes.ATTRIBUTE);
    if (id == null) {
      id = findChildByType(CfscriptTokenTypes.IDENTIFIER);
    }
    if (id != null) {
      return StringUtil.toLowerCase(id.getText());
    }
    return "";
  }

  @Override
  public @Nullable String getPureAttributeValue() {
    PsiElement element = getValueElement();
    if (element != null) {
      final PsiElement[] children = element.getChildren();
      if (children.length > 1) {
        return "";
      }
      return element.getText();
    }

    return "";
  }
}

