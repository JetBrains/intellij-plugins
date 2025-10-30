// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.pom.PomNamedTarget;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

@NotNullByDefault
public final class CucumberJavaLambdaStepPomTarget extends DelegatePsiTarget implements PomNamedTarget {
  public CucumberJavaLambdaStepPomTarget(PsiMethodCallExpression element) {
    super(element);
  }

  @Override
  public @Nullable String getName() {
    final PsiElement element = getNavigationElement();
    // TODO: This code is duplicated from Java8StepDefinition#getCucumberRegexFromElement.
    if (!(element instanceof PsiMethodCallExpression methodCallExpression)) return null;
    final PsiExpressionList argumentList = methodCallExpression.getArgumentList();
    if (argumentList.getExpressions().length <= 1) return null;
    final PsiExpression stepExpression = argumentList.getExpressions()[0];
    final PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(element.getProject()).getConstantEvaluationHelper();
    final Object constantValue = evaluationHelper.computeConstantExpression(stepExpression, false);

    if (constantValue instanceof String string) {
      return string;
    }

    return null;
  }
}
