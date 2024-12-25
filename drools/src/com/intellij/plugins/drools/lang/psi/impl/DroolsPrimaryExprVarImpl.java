// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.DroolsIdentifier;
import com.intellij.plugins.drools.lang.psi.DroolsPrimaryExpr;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.beanProperties.BeanPropertyElement;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class DroolsPrimaryExprVarImpl extends DroolsPsiCompositeElementImpl implements DroolsPrimaryExpr {

  public DroolsPrimaryExprVarImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull PsiType getType() {
    final List<DroolsIdentifier> identifiers = getIdentifierList();
    if (identifiers.size() == 1) {
      //final PsiType type = RecursionManager.doPreventingRecursion(this, true, new Computable<PsiType>() {
      //  @Override
      //  public PsiType compute() {
      //    return resolveType(identifiers.get(0));
      //  }
      //});
      return resolveType(identifiers.get(0));
    }

    return PsiTypes.nullType();
  }

  private static PsiType resolveType(@NotNull DroolsIdentifier identifier) {
    final ResolveResult[] resolveResults = identifier.multiResolve(false);
    for (ResolveResult result : resolveResults) {
      final PsiElement resolve = result.getElement();

      if (resolve instanceof PsiClass) {
        return PsiTypesUtil.getClassType((PsiClass)resolve);
      } else if (resolve instanceof PsiMethod) {
        final PsiType returnType = ((PsiMethod)resolve).getReturnType();
        if (returnType != null) return returnType;
      } if (resolve instanceof PsiField) {
        return ((PsiField)resolve).getType();
      }
      else if (resolve instanceof BeanProperty) {
        return ((BeanProperty)resolve).getPropertyType();
      }
      else if (resolve instanceof BeanPropertyElement) {
        final PsiType propertyType = ((BeanPropertyElement)resolve).getPropertyType();
        if (propertyType != null) return propertyType;
      }
    }
    return PsiTypes.nullType();
  }
}
