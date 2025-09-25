// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.ide.util.EditSourceUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Ref;
import com.intellij.pom.Navigatable;
import com.intellij.pom.PomNamedTarget;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@NotNullByDefault
public final class Java8StepDefinition extends AbstractJavaStepDefinition implements PomNamedTarget {
  private static final Logger LOG = Logger.getInstance(Java8StepDefinition.class);

  public Java8StepDefinition(PsiMethodCallExpression element) {
    super(element);
  }

  public static Java8StepDefinition create(PsiMethodCallExpression element) {
    final Ref<Boolean> usedCache = new Ref<>(true);
    final Java8StepDefinition stepDefinition = CachedValuesManager.getCachedValue(element, () -> {
      usedCache.set(false);
      final Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());
      return CachedValueProvider.Result.create(new Java8StepDefinition(element), document);
    });
    LOG.trace("created for " + element.getText() + ", used cache: " + usedCache.get());
    return stepDefinition;
  }

  @Override
  protected @Nullable String getCucumberRegexFromElement(PsiElement element) {
    // NOTE(bartekpacia): This implementation doesn't conform to this method's name because it can return either a regex or a cukex.
    //  However, it has been like this for many years, and it seems to work fine. If possible, consider refactoring in the future.
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
  public void setValue(String newValue) {
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
    final PsiElement element = super.getElement();
    if (element == null) return null;
    return (PsiMethodCallExpression)element;
  }

  @Override
  public List<String> getVariableNames() {
    return Collections.emptyList(); // This was never implemented. See IDEA-379823.
  }

  @Override
  public @Nullable String getName() {
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

  @Override
  public String toString() {
    return "Java8StepDefinition{backed by element: " + getElement() + "}";
  }
}
