// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.DroolsLhsOOPathBind;
import com.intellij.plugins.drools.lang.psi.DroolsNameId;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class DroolsLhsOOPathBindVariableImpl extends DroolsAbstractVariableImpl implements DroolsLhsOOPathBind {

  public DroolsLhsOOPathBindVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public DroolsNameId getNamedIdElement() {
    return getNameId();
  }

  @NotNull
  @Override
  public PsiType getType() {
    final Set<PsiClass> psiClasses = ContainerUtil.addAllNotNull(DroolsResolveUtil.getPatternOOPathBindType(this.getLhsOOPSegmentList()));
    return psiClasses.isEmpty() ? (PsiPrimitiveType)PsiTypes.nullType()
                                : JavaPsiFacade.getElementFactory(getProject()).createType(psiClasses.iterator().next());
  }
}
