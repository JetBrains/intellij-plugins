/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * Date: 05.11.2008
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

