/*
 * Copyright 2013 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.struts2.annotators;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.model.jam.convention.StrutsConventionConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsConventionImplicitUsageProvider implements ImplicitUsageProvider {

  @Override
  public boolean isImplicitUsage(@NotNull PsiElement element) {
    if (element instanceof PsiClass) {
      return isConventionActionClass((PsiClass)element);
    }

    if (element instanceof PsiMethod psiMethod) {
      if (!checkMethod(psiMethod)) {
        return false;
      }

      return isAnnotatedWithAction(psiMethod) ||
             isConventionActionClass(psiMethod.getContainingClass());
    }
    return false;
  }

  @Override
  public boolean isImplicitRead(@NotNull PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(@NotNull PsiElement element) {
    return false;
  }

  private static boolean checkMethod(PsiMethod psiMethod) {
    return psiMethod.hasModifierProperty(PsiModifier.PUBLIC) &&
           !psiMethod.isConstructor() &&
           !psiMethod.hasModifierProperty(PsiModifier.STATIC) &&
           !psiMethod.hasModifierProperty(PsiModifier.ABSTRACT);
  }

  private static boolean isAnnotatedWithAction(PsiModifierListOwner psiModifierListOwner) {
    return AnnotationUtil.isAnnotated(psiModifierListOwner, StrutsConventionConstants.ACTION, 0);
  }

  private static boolean isConventionActionClass(@Nullable PsiClass psiClass) {
    if (psiClass == null ||
        psiClass.isInterface() ||
        psiClass.isEnum() ||
        psiClass.isAnnotationType() ||
        !psiClass.hasModifierProperty(PsiModifier.PUBLIC) ||
        psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
      return false;
    }

    if (isAnnotatedWithAction(psiClass)) {
      return true;
    }

    if (AnnotationUtil.isAnnotated(psiClass, StrutsConventionConstants.ACTIONS, 0)) {
      return true;
    }

    if (!isConventionPluginPresent(psiClass)) {
      return false;
    }

    if (StringUtil.endsWith(psiClass.getName(), "Action")) {
      return true;
    }

    return InheritanceUtil.isInheritor(psiClass, StrutsConstants.XWORK_ACTION_CLASS);
  }

  private static boolean isConventionPluginPresent(PsiElement element) {
    final PsiClass conventionService = JavaPsiFacade.getInstance(element.getProject()).
      findClass(StrutsConventionConstants.CONVENTIONS_SERVICE, element.getResolveScope());
    return conventionService != null;
  }
}
