// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.DroolsExpression;
import com.intellij.plugins.drools.lang.psi.DroolsNameId;
import com.intellij.plugins.drools.lang.psi.DroolsPrimaryExpr;
import com.intellij.plugins.drools.lang.psi.DroolsUnaryAssignExpr;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public abstract class DroolsUnaryAssignVariableImpl extends DroolsAbstractVariableImpl implements DroolsUnaryAssignExpr {

  public DroolsUnaryAssignVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public PsiType getType() {
    DroolsExpression primaryExpr = getExpression();
    return primaryExpr instanceof DroolsPrimaryExpr ? ((DroolsPrimaryExpr)primaryExpr).getType() : PsiType.getJavaLangObject(PsiManager.getInstance(getProject()), GlobalSearchScope.allScope(getProject()));
  }

  @Override
  protected DroolsNameId getNamedIdElement() {
    return getNameId();
  }
}
