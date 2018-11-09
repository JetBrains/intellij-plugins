// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.manipulators.StringLiteralManipulator;
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
    PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;
    Object value = literalExpression.getValue();
    if (!(value instanceof String)) {
      return PsiReference.EMPTY_ARRAY;
    }

    PsiAnnotation annotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
    if (annotation == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    PsiMethod method = PsiTreeUtil.getParentOfType(annotation, PsiMethod.class);
    if (method == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    String cucumberExpression = CucumberJavaUtil.getStepAnnotationValue(method, null);
    if (cucumberExpression == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    List<CucumberJavaParameterTypeReference> result = new ArrayList<>();
    CucumberUtil.processParameterTypesInCucumberExpression(cucumberExpression, range -> {
      // Skip " in the begin of the String Literal
      range = range.shiftRight(StringLiteralManipulator.getValueRange(literalExpression).getStartOffset());
      result.add(new CucumberJavaParameterTypeReference(element, range));
      return true;
    });
    return result.toArray(new CucumberJavaParameterTypeReference[0]);
  }
}
