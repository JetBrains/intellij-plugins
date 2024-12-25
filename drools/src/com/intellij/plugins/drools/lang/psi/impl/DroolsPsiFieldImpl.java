// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Computable;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.plugins.drools.lang.psi.util.DroolsElementsFactory;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.JavaIdentifier;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class DroolsPsiFieldImpl extends DroolsPsiCompositeElementImpl implements DroolsPsiField, DroolsField {

  private Computable<PsiType> myType;

  public DroolsPsiFieldImpl(@NotNull ASTNode node) {
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
    final DroolsFieldType droolsFieldType = getFieldType();
    if (droolsFieldType != null) {
      DroolsType type = droolsFieldType.getType();
      if (type != null) {
        final PsiType psiType = DroolsResolveUtil.resolveType(type);
        if (psiType != null) return psiType;
      }
      DroolsPrimitiveType primitiveType = droolsFieldType.getPrimitiveType();
      if (primitiveType != null) {
        final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(primitiveType.getProject());
        return elementFactory.createTypeFromText(primitiveType.getText(), primitiveType);
      }
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

  @Override
  public @NotNull List<DroolsAnnotation> getAnnotationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsAnnotation.class);
  }

  @Override
  public @Nullable DroolsConditionalExpr getConditionalExpr() {
    return findChildByClass(DroolsConditionalExpr.class);
  }

  @Override
  public @NotNull DroolsFieldName getFieldName() {
    return findNotNullChildByClass(DroolsFieldName.class);
  }

  @Override
  public @Nullable DroolsFieldType getFieldType() {
    return findChildByClass(DroolsFieldType.class);
  }
}
