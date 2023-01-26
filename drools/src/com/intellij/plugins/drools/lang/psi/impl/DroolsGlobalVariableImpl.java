// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypes;
import org.jetbrains.annotations.NotNull;

public abstract class DroolsGlobalVariableImpl extends DroolsAbstractVariableImpl implements  DroolsGlobalStatement {

  public DroolsGlobalVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public DroolsNameId getNamedIdElement() {
    return getNameId();
  }

  @NotNull
  @Override
  public PsiType getType() {
    final DroolsVarType varType = getVarType();
      final PsiType psiType = DroolsResolveUtil.resolveType(varType.getType());
      if (psiType != null) return psiType;
    return PsiTypes.nullType();
  }
}
