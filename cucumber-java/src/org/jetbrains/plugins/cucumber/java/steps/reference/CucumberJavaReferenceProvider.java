// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.ArrayList;
import java.util.List;

public class CucumberJavaReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    if (!(element instanceof PsiLiteralExpression)) {
      return PsiReference.EMPTY_ARRAY;
    }
    Object value = ((PsiLiteralExpression)element).getValue();
    if (!(value instanceof String)) {
      return PsiReference.EMPTY_ARRAY;
    }

    PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    if (method != null) {
      String cucumberExpression = CucumberJavaUtil.getStepAnnotationValue(method, null);
      if (cucumberExpression == null) {
        return PsiReference.EMPTY_ARRAY;
      }

      List<CucumberJavaParameterTypeReference> result = new ArrayList<>();
      CucumberUtil.processParameterTypesInCucumberExpression(cucumberExpression, range -> {
        range = range.shiftRight(1); // Skip " in the begin of the String Literal

        result.add(new CucumberJavaParameterTypeReference(element, range));
        return true;
      });
      return result.toArray(new CucumberJavaParameterTypeReference[0]);
    }

    return PsiReference.EMPTY_ARRAY;
  }
}
