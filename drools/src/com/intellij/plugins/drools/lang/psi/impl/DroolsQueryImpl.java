// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.DroolsParameters;
import com.intellij.plugins.drools.lang.psi.DroolsQuery;
import com.intellij.plugins.drools.lang.psi.DroolsQueryStatement;
import com.intellij.plugins.drools.lang.psi.DroolsStringId;
import com.intellij.plugins.drools.lang.psi.util.DroolsElementsFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiTarget;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.light.JavaIdentifier;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class DroolsQueryImpl extends DroolsFakePsiMethod  implements DroolsQuery, DroolsQueryStatement, PsiTarget {

  public DroolsQueryImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String getQueryName() {
    return getStringId().getText();
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    final DroolsStringId oldIdentifier = getStringId();

    final PsiElement patternBindIdentifier = DroolsElementsFactory.createQueryNameIdentifier(name, getProject());
    if (patternBindIdentifier != null) {
      oldIdentifier.replace(patternBindIdentifier);
    }
    return this;
  }

  @Override
  @NotNull
  public String getName() {
    return getQueryName();
  }

  @Override
  public PsiIdentifier getNameIdentifier() {
    return new JavaIdentifier(getManager(), getStringId());
  }

  @Override
  public PsiType getReturnType() {
    return PsiType.NULL;
  }

  @Override
  protected DroolsParameters getDroolsParameters() {
    return getQueryParameters();
  }
}
