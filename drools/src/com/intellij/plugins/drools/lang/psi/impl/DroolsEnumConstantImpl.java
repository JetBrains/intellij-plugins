// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Computable;
import com.intellij.plugins.drools.lang.psi.DroolsEnumConstant;
import com.intellij.plugins.drools.lang.psi.DroolsFieldName;
import com.intellij.plugins.drools.lang.psi.DroolsPsiClass;
import com.intellij.plugins.drools.lang.psi.util.DroolsElementsFactory;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.JavaIdentifier;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public   class DroolsEnumConstantImpl extends DroolsPsiFieldImpl implements DroolsEnumConstant {

  private Computable<PsiType> myType;

  public DroolsEnumConstantImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull String getName() {
    return getFieldName().getText();
  }

  @Override
  public void setInitializer(@Nullable PsiExpression initializer) throws IncorrectOperationException {
  }

  @Override
  public @NotNull PsiIdentifier getNameIdentifier() {
    return new JavaIdentifier(getManager(), getFieldName());
  }

  @Override
  public @Nullable PsiDocComment getDocComment() {
    return null;
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @Override
  public @Nullable PsiClass getContainingClass() {
    return PsiTreeUtil.getParentOfType(this, DroolsPsiClass.class);
  }

  @Override
  public @NotNull PsiType getType() {
    final DroolsPsiClass psiClass = PsiTreeUtil.getParentOfType(this, DroolsPsiClass.class);
    if (psiClass != null) {
      // todo
    }
    return PsiType.getJavaLangObject(getManager(), getResolveScope());
  }

  @Override
  public @Nullable PsiTypeElement getTypeElement() {
    return null;
  }

  @Override
  public @Nullable PsiExpression getInitializer() {
    return null;
  }

  @Override
  public boolean hasInitializer() {
    return false;
  }

  @Override
  public void normalizeDeclaration() throws IncorrectOperationException {

  }

  @Override
  public @Nullable Object computeConstantValue() {
    return null;
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    final DroolsFieldName oldIdentifier = getFieldName();

    final PsiElement nameIdentifier = DroolsElementsFactory.createFieldNameIdentifier(name, getProject());
    if (nameIdentifier != null) {
      oldIdentifier.replace(nameIdentifier);
    }
    return this;
  }

  @Override
  public @Nullable PsiModifierList getModifierList() {
    return null;
  }

  @Override
  public boolean hasModifierProperty(@PsiModifier.ModifierConstant @NonNls @NotNull String name) {
    return false;
  }
}
