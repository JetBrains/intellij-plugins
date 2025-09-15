// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Java8StepDefinition extends AbstractJavaStepDefinition {
  public Java8StepDefinition(@NotNull PsiElement element) {
    super(element);
  }

  @Override
  protected @Nullable String getCucumberRegexFromElement(PsiElement element) {
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

  @Override
  public void setValue(@NotNull String newValue) {
    if (!(getElement() instanceof PsiMethodCallExpression methodCallExpression)) return;
    final PsiExpressionList argumentList = methodCallExpression.getArgumentList();
    if (argumentList.getExpressions().length <= 1) return;
    final PsiExpression stepExpression = argumentList.getExpressions()[0];
    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(getElement().getProject());
    PsiExpression newFirstArgument = factory.createExpressionFromText("\"" + newValue + "\"", null);
    stepExpression.replace(newFirstArgument);
  }
}
