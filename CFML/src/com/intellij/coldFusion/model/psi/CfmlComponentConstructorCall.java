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
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class CfmlComponentConstructorCall extends CfmlFunctionCallExpression implements CfmlFunctionCall {
  public CfmlComponentConstructorCall(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public CfmlComponentReference getReferenceExpression() {
    PsiElement childByType = findChildByType(CfmlElementTypes.COMPONENT_REFERENCE);
    if (childByType == null) {
      CfmlStringLiteralExpression childrenByClass = findChildByClass(CfmlStringLiteralExpression.class);
      if (childrenByClass != null) {
        childByType = childrenByClass.getValueElement();
      }
    }
    if (childByType != null) {
      ASTNode node = childByType.getNode();
      if (node != null) {
        return new CfmlComponentReference(node, this) {
          @Override
          public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
            ResolveResult[] resolveResults = super.multiResolve(incompleteCode);
            List<CfmlFunction> result = new LinkedList<>();
            for (ResolveResult resolveResult : resolveResults) {
              PsiElement element = resolveResult.getElement();
              if (element instanceof CfmlComponent component) {
                CfmlFunction[] functions = component.getFunctions();
                for (CfmlFunction function : functions) {
                  if ("init".equalsIgnoreCase(function.getName())) {
                    result.add(function);
                  }
                }
              }
            }
            return CfmlResolveResult.create(result);
          }
        };
      }
    }
    return null;
  }

  @Override
  public PsiType getPsiType() {
    CfmlReference referenceExpression = getReferenceExpression();

    return referenceExpression != null ? referenceExpression.getPsiType() : null;
  }
}
