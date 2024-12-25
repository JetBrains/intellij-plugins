// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

  @Override
  public @Nullable CfmlComponentReference getReferenceExpression() {
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
