// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NotNullByDefault
public final class Java8StepDefinition extends AbstractJavaStepDefinition {

  public Java8StepDefinition(PsiMethodCallExpression element, Module module) {
    super(element, module);
  }

  @Override
  protected @Nullable String getCucumberRegexFromElement(@Nullable PsiElement element) {
    // NOTE(bartekpacia): This implementation doesn't conform to this method's name because it can return either a regex or a cukex.
    //  However, it has been like this for many years, and it seems to work fine. If possible, consider refactoring in the future.
    if (!(element instanceof PsiMethodCallExpression methodCallExpression)) return null;
    return CucumberJavaUtil.getJava8StepName(methodCallExpression);
  }

  @Override
  public void setValue(String newValue) {
    if (!(getElement() instanceof PsiMethodCallExpression methodCallExpression)) return;
    final PsiExpression stepExpression = CucumberJavaUtil.getJava8StepNameExpression(methodCallExpression);
    if (stepExpression == null) return;
    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(getElement().getProject());
    PsiExpression newFirstArgument = factory.createExpressionFromText("\"" + newValue + "\"", null);
    stepExpression.replace(newFirstArgument);
  }

  @Override
  public @Nullable PsiMethodCallExpression getElement() {
    final PsiElement element = super.getElement();
    if (element == null) return null;
    return (PsiMethodCallExpression)element;
  }

  @Override
  public List<String> getVariableNames() {
    final PsiMethodCallExpression methodCall = getElement();
    if (methodCall == null) {
      return Collections.emptyList();
    }

    final PsiExpression[] arguments = methodCall.getArgumentList().getExpressions();
    if (arguments.length < 2) {
      return Collections.emptyList();
    }

    final PsiExpression lambdaArg = arguments[1];
    if (!(lambdaArg instanceof PsiLambdaExpression lambda)) {
      return Collections.emptyList();
    }

    final PsiParameterList parameterList = lambda.getParameterList();
    final PsiParameter[] parameters = parameterList.getParameters();
    final List<String> result = new ArrayList<>();
    for (final PsiParameter parameter : parameters) {
      result.add(parameter.getName());
    }
    return result;
  }

  @Override
  public String toString() {
    return "Java8StepDefinition{backed by element: " + getElement() + "}";
  }
}
