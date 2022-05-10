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

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.CfmlProperty;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 */
public class CfmlTagPropertyImpl extends CfmlNamedTagImpl implements CfmlProperty {
  public CfmlTagPropertyImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  public PsiElement getNameIdentifier() {
    return getAttributeValueElement("name");
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }

  @Override
  @Nullable
  public CfmlComponent getComponent() {
    return PsiTreeUtil.findChildOfType(this, CfmlComponent.class);
  }

  private boolean checkBooleanAttribute(String attributeName) {
    PsiElement attributeValue = getAttributeValueElement(attributeName);
    if (attributeValue != null) {
      if ("true".equalsIgnoreCase(attributeValue.getText())) {
        return true;
      }
      return false;
    }

    CfmlComponent component = getComponent();
    return component != null ? component.hasImplicitAccessors() : false;
  }

  @Override
  public boolean hasGetter() {
    return checkBooleanAttribute("getter");
  }

  @Override
  public boolean hasSetter() {
    return checkBooleanAttribute("setter");
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }

  @NotNull
  @Override
  public String getlookUpString() {
    return getName();
  }
}
