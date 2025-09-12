// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.ide.util.EditSourceUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.pom.Navigatable;
import com.intellij.pom.PomNamedTarget;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Java8StepDefinition extends AbstractJavaStepDefinition implements PomNamedTarget {
  public Java8StepDefinition(@NotNull PsiMethodCallExpression element) {
    super(element);
  }

  public static Java8StepDefinition create(@NotNull PsiMethodCallExpression element) {
    return CachedValuesManager.getCachedValue(element, () -> {
      final Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());
      return CachedValueProvider.Result.create(new Java8StepDefinition(element), document);
    });
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

  @Override
  public @Nullable PsiMethodCallExpression getElement() {
    return (PsiMethodCallExpression)super.getElement();
  }

  @Override
  public String getName() {
    return getExpression();
  }

  @Override
  public boolean isValid() {
    final PsiElement element = getElement();
    return element != null && element.isValid();
  }

  @Override
  public void navigate(boolean requestFocus) {
    final PsiElement element = getElement();
    if (element == null) return;
    final Navigatable navigatable = EditSourceUtil.getDescriptor(element);
    if (navigatable == null) return;
    navigatable.navigate(requestFocus);
  }

  @Override
  public boolean canNavigate() {
    return EditSourceUtil.canNavigate(getElement());
  }

  @Override
  public boolean canNavigateToSource() {
    return canNavigate();
  }
}
