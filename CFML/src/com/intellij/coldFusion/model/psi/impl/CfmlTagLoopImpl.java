// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

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

    @Override
    public @NotNull String getName() {
      return getNameIdentifier().getText();
    }

    @Override
    public boolean isTrulyDeclaration() {
      return true;
    }

    @Override
    public @NotNull String getlookUpString() {
      return getName();
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
      throw new IncorrectOperationException();
    }

    @Override
    public @NotNull PsiElement getNavigationElement() {
      return super.getNavigationElement();
    }

    @Override
    public int getTextOffset() {
      PsiElement element = getNavigationElement();
      return element.getTextRange().getStartOffset();
    }

    @Override
    public @NotNull PsiElement getNameIdentifier() {
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

  @Override
  public @NotNull String getTagName() {
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
