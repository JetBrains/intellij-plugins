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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 * @date 09.02.11
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

  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    CheckUtil.checkWritable(this);
    CfmlAttributeNameImpl childByClass = findChildByClass(CfmlAttributeNameImpl.class);
    if (childByClass != null) {
      childByClass.setName(name);
    }
    return this;
  }
}
