// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.manipulators.StringLiteralManipulator;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.ArrayList;
import java.util.List;

@NotNullByDefault
public final class CucumberJavaReferenceProvider extends PsiReferenceProvider {
  @Override
  public boolean acceptsTarget(PsiElement target) {
    return target instanceof PomTargetPsiElement pomTarget && pomTarget.getTarget() instanceof CucumberJavaParameterPomTarget;
  }

  @Override
  public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
    if (!(element instanceof PsiLiteralExpression literalExpression)) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (!(literalExpression.getValue() instanceof String literalValue) || !CucumberUtil.isCucumberExpression(literalValue)) {
      // Custom `ParameterType`s can only be used in Cucumber expressions.
      return PsiReference.EMPTY_ARRAY;
    }

    if (isAnnotationStep(literalExpression) || isJava8Step(literalExpression)) {
      final List<CucumberJavaParameterTypeReference> result = new ArrayList<>();
      CucumberUtil.processParameterTypesInCucumberExpression(literalValue, range -> {
        // Skip " at the beginning of the string literal
        range = range.shiftRight(StringLiteralManipulator.getValueRange(literalExpression).getStartOffset());
        result.add(new CucumberJavaParameterTypeReference(literalExpression, range));
        return true;
      });
      return result.toArray(new CucumberJavaParameterTypeReference[0]);
    }

    return PsiReference.EMPTY_ARRAY;
  }

  private static boolean isAnnotationStep(PsiLiteralExpression literalExpression) {
    final PsiMethod method = PsiTreeUtil.getParentOfType(literalExpression, PsiMethod.class);
    if (method == null) return false;
    return CucumberJavaUtil.isAnnotationStepDefinition(method);
  }

  private static boolean isJava8Step(PsiLiteralExpression literalExpression) {
    final PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(literalExpression, PsiMethodCallExpression.class);
    if (methodCallExpression == null) return false;
    return CucumberJavaUtil.isJava8StepDefinition(methodCallExpression);
  }
}
