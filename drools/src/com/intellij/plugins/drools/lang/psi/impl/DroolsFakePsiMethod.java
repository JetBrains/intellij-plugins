// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.plugins.drools.lang.psi.DroolsLightParameter;
import com.intellij.plugins.drools.lang.psi.DroolsParameter;
import com.intellij.plugins.drools.lang.psi.DroolsParameters;
import com.intellij.plugins.drools.lang.psi.DroolsPrimitiveType;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.impl.PsiSuperMethodImplUtil;
import com.intellij.psi.impl.light.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class DroolsFakePsiMethod extends DroolsPsiCompositeElementImpl implements PsiMethod {
  private PsiModifierList myModifierList;
  private LightTypeParameterListBuilder myTypeParameterList;
  private PsiReferenceList myThrowsList;

  public DroolsFakePsiMethod(@NotNull ASTNode node) {
    super(node);
  }

  private @NotNull PsiParameter createParameter(@NotNull DroolsParameter droolsParameter) {
    LightParameter parameter;
    final String paramName = droolsParameter.getNameId().getText();
    final PsiType psiType = DroolsResolveUtil.resolveType(droolsParameter.getType());

    if (psiType == null) {
      DroolsPrimitiveType primitiveType = droolsParameter.getPrimitiveType();
      if (primitiveType != null) {
        final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(primitiveType.getProject());
        PsiType type = elementFactory.createTypeFromText(primitiveType.getText(), primitiveType);
        parameter = new DroolsLightParameter(droolsParameter, paramName, type, this);
      }
      else {
        parameter = new DroolsLightParameter(droolsParameter, paramName, PsiTypes.nullType(), this);
      }
    }
    else {
      parameter = new DroolsLightParameter(droolsParameter, paramName, psiType, this);
    }

    parameter.setNavigationElement(droolsParameter);

    return parameter;
  }

  @Override
  public PsiTypeElement getReturnTypeElement() {
    return null;
  }

  @Override
  public @NotNull PsiParameterList getParameterList() {
    LightParameterListBuilder builder = new LightParameterListBuilder(getManager(), JavaLanguage.INSTANCE);
    final DroolsParameters parameters = getDroolsParameters();
    if (parameters != null) {
      for (DroolsParameter droolsParameter : parameters.getParameterList()) {
        builder.addParameter(createParameter(droolsParameter));
      }
    }
    return builder;
  }

  protected abstract DroolsParameters getDroolsParameters();

  @Override
  public @NotNull PsiReferenceList getThrowsList() {
    if (myThrowsList == null) {
      myThrowsList = new LightReferenceListBuilder(getManager(), JavaLanguage.INSTANCE, PsiReferenceList.Role.THROWS_LIST);
    }
    return myThrowsList;
  }

  @Override
  public PsiCodeBlock getBody() {
    return null;
  }

  @Override
  public boolean isConstructor() {
    return false;
  }

  @Override
  public boolean isVarArgs() {
    return false;
  }

  @Override
  public @NotNull MethodSignature getSignature(@NotNull PsiSubstitutor substitutor) {
    return MethodSignatureBackedByPsiMethod.create(this, substitutor);
  }



  @Override
  public PsiMethod @NotNull [] findSuperMethods() {
    return PsiSuperMethodImplUtil.findSuperMethods(this);
  }

  @Override
  public PsiMethod @NotNull [] findSuperMethods(boolean checkAccess) {
    return PsiSuperMethodImplUtil.findSuperMethods(this, checkAccess);
  }

  @Override
  public PsiMethod @NotNull [] findSuperMethods(PsiClass parentClass) {
    return PsiSuperMethodImplUtil.findSuperMethods(this, parentClass);
  }

  @Override
  public @NotNull List<MethodSignatureBackedByPsiMethod> findSuperMethodSignaturesIncludingStatic(boolean checkAccess) {
    return PsiSuperMethodImplUtil.findSuperMethodSignaturesIncludingStatic(this, checkAccess);
  }

  @Override
  public PsiMethod findDeepestSuperMethod() {
    return PsiSuperMethodImplUtil.findDeepestSuperMethod(this);
  }

  @Override
  public PsiMethod @NotNull [] findDeepestSuperMethods() {
    return PsiSuperMethodImplUtil.findDeepestSuperMethods(this);
  }

  @Override
  public @NotNull PsiModifierList getModifierList() {
    if (myModifierList == null) {
      myModifierList = new LightModifierList(getManager());
    }
    return myModifierList;
  }

  @Override
  public boolean hasModifierProperty(@PsiModifier.ModifierConstant @NonNls @NotNull String name) {
    return getModifierList().hasModifierProperty(name);
  }

  @Override
  public PsiDocComment getDocComment() {
    return null;
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @Override
  public PsiTypeParameterList getTypeParameterList() {
    if (myTypeParameterList == null) {
      myTypeParameterList = new LightTypeParameterListBuilder(getManager(), JavaLanguage.INSTANCE);
    }
    return myTypeParameterList;
  }

  @Override
  public boolean hasTypeParameters() {
    return PsiImplUtil.hasTypeParameters(this);
  }

  @Override
  public PsiTypeParameter @NotNull [] getTypeParameters() {
    return PsiImplUtil.getTypeParameters(this);
  }

  @Override
  public PsiClass getContainingClass() {
    return null;
  }

  @Override
  public @NotNull HierarchicalMethodSignature getHierarchicalMethodSignature() {
    return PsiSuperMethodImplUtil.getHierarchicalMethodSignature(this);
  }
}
