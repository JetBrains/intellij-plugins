// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Java8StepDefinition extends AbstractJavaStepDefinition {
  public Java8StepDefinition(@NotNull PsiElement element, @NotNull Module module) {
    super(element, module);
  }

  @Override
  protected @Nullable String getCucumberRegexFromElement(PsiElement element) {
    if (!(element instanceof PsiMethodCallExpression)) {
      return null;
    }
    PsiExpressionList argumentList = ((PsiMethodCallExpression)element).getArgumentList();
    if (argumentList.getExpressions().length <= 1) {
      return null;
    }
    PsiExpression stepExpression = argumentList.getExpressions()[0];
    final PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(element.getProject()).getConstantEvaluationHelper();
    final Object constantValue = evaluationHelper.computeConstantExpression(stepExpression, false);

    if (constantValue != null) {
      if (constantValue instanceof String) {
        return (String)constantValue;
      }
    }
    return null;
  }
}
