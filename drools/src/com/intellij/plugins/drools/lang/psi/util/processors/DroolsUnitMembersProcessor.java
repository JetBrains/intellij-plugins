// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.util.DroolsBeanPropertyLightVariable;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PropertyUtilBase;
import org.jetbrains.annotations.NotNull;

public final class DroolsUnitMembersProcessor implements DroolsDeclarationsProcessor {
  private static DroolsUnitMembersProcessor myInstance;

  private DroolsUnitMembersProcessor() {
  }

  public static DroolsUnitMembersProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsUnitMembersProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull final DroolsFile droolsFile) {
    final PsiClass unitClass = DroolsResolveUtil.getUnitClass(droolsFile);
    if (unitClass != null) {
      for (PsiMethod method : unitClass.getAllMethods()) {
        if (!processor.execute(method, state)) return false;
      }
      for (PsiMethod psiMethod : PropertyUtilBase.getAllProperties(unitClass, false, true).values()) {
        final BeanProperty beanProperty = BeanProperty.createBeanProperty(psiMethod);
        if (beanProperty != null && !processor.execute(new DroolsBeanPropertyLightVariable(beanProperty), state)) return false;
      }
    }

    return true;
  }
}
