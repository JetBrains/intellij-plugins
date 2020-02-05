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

import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author vnikolaenko
 * @date 09.02.11
 */
public class CfmlPropertyImpl extends CfmlCompositeElement implements CfmlProperty {
  public CfmlPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  public String getName() {
    PsiElement nameIdentifier = getNameIdentifier();
    return nameIdentifier != null ? nameIdentifier.getText() : "";
  }

  @Override
  public PsiElement getNameIdentifier() {
    PsiElement lastChild = getLastChild();
    if (lastChild == null) {
      return null;
    }
    ASTNode node = lastChild.getNode();
    if (node != null && node.getElementType() == CfscriptTokenTypes.IDENTIFIER) {
      return lastChild;
    }
    else {
      CfmlNamedAttributeImpl namedAttribute = PsiTreeUtil.findChildOfType(this, CfmlNamedAttributeImpl.class);
      if (namedAttribute != null) {
        return namedAttribute.getNameIdentifier();
      }
    }
    return null;
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }

  private boolean checkAccessorAttribute(String accessorName) {
    CfmlAttributeImpl attributeValue = getNamedAttribute(accessorName);
    if (attributeValue != null) {
      if ("true".equalsIgnoreCase(attributeValue.getPureAttributeValue())) {
        return true;
      }
      return false;
    }

    CfmlComponent component = getComponent();
    return component != null && component.hasImplicitAccessors();
  }

  @Override
  public boolean hasGetter() {
    return checkAccessorAttribute("getter");
  }

  @Override
  public boolean hasSetter() {
    return checkAccessorAttribute("setter");
  }

  private CfmlAttributeImpl getNamedAttribute(String attributeName) {
    Collection<CfmlAttributeImpl> namedAttribute = PsiTreeUtil.findChildrenOfType(this, CfmlAttributeImpl.class);
    for (CfmlAttributeImpl attribute : namedAttribute) {
      if (attributeName.equals(attribute.getName())) {
        return attribute;
      }
    }
    return null;
  }
  public CfmlAttributeImpl getTypeIdentifier() {
    return getNamedAttribute("type");
  }

  @Override
  public String getType() {
    CfmlAttributeImpl identifier = getTypeIdentifier();
    return identifier != null ? identifier.getPureAttributeValue() : null;
  }

  public CfmlAttributeImpl getDefaultIdentifier() {
    return getNamedAttribute("default");
  }
  
  @Override
  public String getDefault() {
    CfmlAttributeImpl identifier = getDefaultIdentifier();
    return identifier != null ? identifier.getPureAttributeValue() : null;
  }

  @Override
  public CfmlComponent getComponent() {
    return PsiTreeUtil.getParentOfType(this, CfmlComponent.class);
  }

  @NotNull
  @Override
  public String getlookUpString() {
    String name = getName();
    return name != null ? name : "";
  }
}
