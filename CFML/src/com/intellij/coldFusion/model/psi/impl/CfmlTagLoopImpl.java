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
import com.intellij.coldFusion.model.psi.CfmlRecursiveElementVisitor;
import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiType;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author: vnikolaenko
 */
public class CfmlTagLoopImpl extends CfmlTagImpl {

  public CfmlTagLoopImpl(ASTNode astNode) {
    super(astNode);
  }

  public static class Variable extends CfmlAttributeNameImpl implements CfmlVariable {
    public Variable(@NotNull ASTNode node) {
      super(node);
    }

    @Override
    public PsiType getPsiType() {
      return null;
    }

    @NotNull
    @Override
    public String getName() {
      return getNameIdentifier().getText();
    }

    @Override
    public boolean isTrulyDeclaration() {
      return true;
    }

    @NotNull
    @Override
    public String getlookUpString() {
      return getName();
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
      throw new IncorrectOperationException();
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
      PsiElement element = findChildByType(CfmlElementTypes.ATTRIBUTE_VALUE);
      return element != null ? element : this;
    }

    public int getTextOffset() {
      PsiElement element = getNavigationElement();
      return element.getTextRange().getStartOffset();
    }

    @NotNull
    public PsiElement getNameIdentifier() {
      return getNavigationElement();
    }
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    Variable referenceDefinition = findChildByClass(Variable.class);
    if (referenceDefinition != null) {
      if (!processor.execute(referenceDefinition, state)) {
        return false;
      }
    }
    return CfmlPsiUtil.processDeclarations(processor, state, lastParent, this);
  }

  @NotNull
  public String getTagName() {
    return "cfloop";
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CfmlRecursiveElementVisitor) {
      ((CfmlRecursiveElementVisitor)visitor).visitCfmlTag(this);
    }
    else {
      super.accept(visitor);
    }
  }
}
