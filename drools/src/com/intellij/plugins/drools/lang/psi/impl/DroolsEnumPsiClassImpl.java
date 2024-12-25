// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class DroolsEnumPsiClassImpl extends DroolsPsiClassImpl implements DroolsPsiClass, DroolsEnumDeclaration {

  public DroolsEnumPsiClassImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String getName() {
    return getTypeName().getText();
  }

  @Override
  public @Nullable String getQualifiedName() {
    String typeName = getTypeName().getText();
    if (StringUtil.getPackageName(typeName).isEmpty()) {
      String aPackage = DroolsResolveUtil.getCurrentPackage(PsiTreeUtil.getParentOfType(this, DroolsFile.class));
      typeName =  aPackage +"." +typeName;
    }
    return typeName;
  }

  @Override
  public boolean isEnum() {
    return true;
  }

  @Override
  public PsiField @NotNull [] getFields() {
    return findChildrenByClass(DroolsEnumConstant.class);
  }

  @Override
  public PsiClassType @NotNull [] getExtendsListTypes() {
    return PsiClassType.EMPTY_ARRAY;
  }

  @Override
  public PsiClassType @NotNull [] getImplementsListTypes() {
    return PsiClassType.EMPTY_ARRAY;
  }

  @Override
  public @Nullable PsiClass getSuperClass() {
    return null;
  }

  @Override
  public PsiClass @NotNull [] getInterfaces() {
    return PsiClass.EMPTY_ARRAY;
  }
  public void accept(@NotNull DroolsVisitor visitor) {
    visitor.visitTypeDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DroolsVisitor) accept((DroolsVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull List<DroolsAnnotation> getAnnotationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsAnnotation.class);
  }

  @Override
  public @NotNull List<DroolsField> getFieldList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsField.class);
  }

  @Override
  public @Nullable DroolsSuperType getSuperType() {
    return findChildByClass(DroolsSuperType.class);
  }

  @Override
  public @Nullable DroolsTraitable getTraitable() {
    return findChildByClass(DroolsTraitable.class);
  }

  @Override
  public @NotNull DroolsTypeName getTypeName() {
    return findNotNullChildByClass(DroolsTypeName.class);
  }
}
