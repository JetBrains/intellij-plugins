// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.DroolsLhsOOPSegment;
import com.intellij.plugins.drools.lang.psi.util.DroolsBeanPropertyLightVariable;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightVariable;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PropertyUtilBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.plugins.drools.DroolsConstants.DATA_STORE_CLASS;

public final class DroolsOopSegmentProcessor implements DroolsDeclarationsProcessor {
  private static DroolsOopSegmentProcessor myInstance;

  private DroolsOopSegmentProcessor() {
  }

  public static DroolsOopSegmentProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsOopSegmentProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, final @NotNull DroolsFile droolsFile) {

    final DroolsLhsOOPSegment oopSegment = PsiTreeUtil.getParentOfType(place, DroolsLhsOOPSegment.class);
    if (oopSegment != null) {
      final String name = oopSegment.getLhsOOPathSegmentId().getName();
      if (StringUtil.isNotEmpty(name)) {
        final PsiClass unitClass = DroolsResolveUtil.getUnitClass(droolsFile);
        if (unitClass != null) {
          for (PsiMethod psiMethod : PropertyUtilBase.getAllProperties(unitClass, false, true).values()) {
            final BeanProperty beanProperty = BeanProperty.createBeanProperty(psiMethod);
            if (beanProperty != null && name.equals(beanProperty.getName())) {
              final PsiType dataStoreClass = PsiUtil.substituteTypeParameter(beanProperty.getPropertyType(), DATA_STORE_CLASS, 0, false);
              if (dataStoreClass != null) {
                if (!processor.execute(new DroolsLightVariable(beanProperty.getName(), dataStoreClass, beanProperty.getPsiElement()),
                                       state)) {
                  return false;
                }
              }
              else {
                if (!processor.execute(new DroolsBeanPropertyLightVariable(beanProperty), state)) return false;
              }
            }
          }
        }
      }
    }

    return true;
  }
}
