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
import com.intellij.coldFusion.model.psi.CfmlAssignmentExpression;
import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author vnikolaenko
 */
public class CfmlForImpl extends CfmlCompositeElement {

  public static class Variable extends CfmlCompositeElement implements CfmlVariable {
    public Variable(@NotNull ASTNode node) {
      super(node);
    }

    @Override
    public PsiType getPsiType() {
      return null;
    }

    @Override
    public String getName() {
      return getText();
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
    public PsiElement getNameIdentifier() {
      return this;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
      throw new IncorrectOperationException();
    }
  }

  public CfmlForImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if (findChildByType(CfscriptTokenTypes.IN_L) != null) {
      Variable referenceDefinition = findChildByClass(Variable.class);
      if (referenceDefinition != null) {
        if (!processor.execute(referenceDefinition, state)) {
          return false;
        }
      }
    }
    else {
      List<PsiElement> childrenByType = findChildrenByType(CfscriptTokenTypes.SEMICOLON);
      if (childrenByType.size() == 2) {
        CfmlAssignmentExpression[] childrenByClass = findChildrenByClass(CfmlAssignmentExpression.class);
        for (CfmlAssignmentExpression assignment : childrenByClass) {
          if (assignment.getTextOffset() <= childrenByType.get(0).getTextOffset()) {
            if (assignment.getAssignedVariable() != null && !processor.execute(assignment.getAssignedVariable(), state)) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }
}
