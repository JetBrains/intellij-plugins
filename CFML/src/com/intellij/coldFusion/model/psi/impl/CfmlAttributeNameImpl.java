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

  @NotNull
  public String getName() {
    final String name = getPureAttributeValue();
    return name != null ? name : "";
  }
}
