// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlAttribute;
import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.lang.ASTNode;
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

  @Nullable
  public PsiElement getValueElement() {
    return findChildByType(CfmlElementTypes.ATTRIBUTE_VALUE);
  }

  @Override
  public String getAttributeName() {
    PsiElement id = findChildByType(CfmlTokenTypes.ATTRIBUTE);
    if (id == null) {
      id = findChildByType(CfscriptTokenTypes.IDENTIFIER);
    }
    if (id != null) {
      return id.getText().toLowerCase();
    }
    return "";
  }

  @Override
  @Nullable
  public String getPureAttributeValue() {
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

