// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.manipulators.StringLiteralManipulator;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
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

    if (!(literalExpression.getValue() instanceof String)) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (isCucumberAnnotationStep(literalExpression) || isCucumberLambdaStep(literalExpression)) {
      final List<CucumberJavaParameterTypeReference> result = new ArrayList<>();
      CucumberUtil.processParameterTypesInCucumberExpression(literalExpression.getValue().toString(), range -> {
        // Skip " at the beginning of the string literal
        range = range.shiftRight(StringLiteralManipulator.getValueRange(literalExpression).getStartOffset());
        result.add(new CucumberJavaParameterTypeReference(literalExpression, range));
        return true;
      });
      return result.toArray(new CucumberJavaParameterTypeReference[0]);
    }

    return PsiReference.EMPTY_ARRAY;
  }

  private static boolean isCucumberAnnotationStep(PsiLiteralExpression literalExpression) {
    final PsiAnnotation annotation = PsiTreeUtil.getParentOfType(literalExpression, PsiAnnotation.class);
    if (annotation == null) return false;
    if (!CucumberJavaUtil.isCucumberStepAnnotation(annotation)) return false;
    final String cucumberExpression = CucumberJavaUtil.getAnnotationValue(annotation);
    if (cucumberExpression == null) return false;
    return true;
  }

  private static boolean isCucumberLambdaStep(PsiLiteralExpression literalExpression) {
    final PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(literalExpression, PsiMethodCallExpression.class);
    if (methodCallExpression == null) return false;
    PsiExpressionList argumentList = methodCallExpression.getArgumentList();
    if (argumentList.getExpressionCount() != 2) return false;
    // Optimization to avoid resolve(): must have 2 arguments, first argument must be a String
    final PsiMethod method = methodCallExpression.resolveMethod();
    if (method == null) return false;

    final PsiClass classOfMethod = method.getContainingClass();
    if (classOfMethod == null) return false;

    final boolean isCucumberLambdaMethod = ContainerUtil.exists(classOfMethod.getInterfaces(), psiClass -> {
      final String fqn = psiClass.getQualifiedName();
      return fqn != null && (fqn.equals("cucumber.api.java8.LambdaGlue") || fqn.equals("io.cucumber.java8.LambdaGlue"));
    });

    if (!isCucumberLambdaMethod) return false;
    return true;
  }
}
