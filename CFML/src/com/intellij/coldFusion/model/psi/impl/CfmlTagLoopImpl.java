// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
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
      PsiElement parent = getParent();
      if (!(parent instanceof CfmlTagLoopImpl)) return null;
      PsiElement arrayReference = ((CfmlTagLoopImpl)parent).getArrayReferenceExpression();
      if (!(arrayReference instanceof CfmlTypedElement)) return null;
      PsiType type = ((CfmlTypedElement)arrayReference).getPsiType();
      return type instanceof PsiArrayType ? ((PsiArrayType)type).getComponentType() : null;
    }

    @NotNull
    @Override
    public String getName() {
      String name = getNameIdentifier().getText();
      if (name.startsWith("local.")) {
        name = name.substring(6);
      }
      return name;
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

    @Override
    public int getTextOffset() {
      PsiElement element = getNavigationElement();
      return element.getTextRange().getStartOffset();
    }

    @Override
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
  
   CfmlExpression getArrayReferenceExpression () {
     PsiElement valueElement = getAttributeValueElement("array");
     if (valueElement == null) return null;
     for (PsiElement cur = valueElement.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
       if (cur instanceof CfmlExpression) return (CfmlExpression)cur;
     }
     return null;
  }

  @Override
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
