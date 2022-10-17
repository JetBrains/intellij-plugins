// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.plugins.drools.lang.psi.util.DroolsElementsFactory;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.JavaIdentifier;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class DroolsFunctionImpl extends DroolsFakePsiMethod implements DroolsFunction, DroolsFunctionStatement,
                                                                                          PsiTarget {
  private final NotNullLazyValue<PsiType> myReturnType = NotNullLazyValue.lazy(
    () -> {
      final DroolsType type = getType();
      if (type != null) {
        final PsiType psiType = DroolsResolveUtil.resolveType(type);
        if (psiType != null) return psiType;
      }
      DroolsPrimitiveType primitiveType = getPrimitiveType();
      if (primitiveType != null) {
        final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(primitiveType.getProject());
        return elementFactory.createTypeFromText(primitiveType.getText(), primitiveType);
      }
      return PsiType.NULL;
    }
  );

  public DroolsFunctionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String getFunctionName() {
    return getNameId().getText();
  }

  @Override
  public String getName() {
    return getNameId().getText();
  }

  @Override
  public PsiIdentifier getNameIdentifier() {
    return new JavaIdentifier(getManager(), getNameId());
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    final DroolsNameId oldIdentifier = getNameId();

    final PsiElement patternBindIdentifier = DroolsElementsFactory.createFunctionNameIdentifier(name, getProject());
    if (patternBindIdentifier != null) {
      oldIdentifier.replace(patternBindIdentifier);
    }
    return this;
  }

  @Override
  public PsiType getReturnType() {
    return myReturnType.getValue();
  }

  @Override
  protected DroolsParameters getDroolsParameters() {
    return getFunctionParameters();
  }

}
