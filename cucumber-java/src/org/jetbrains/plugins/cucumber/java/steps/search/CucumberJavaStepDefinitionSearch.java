// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaLambdaStepPomTarget;

import java.util.List;

@NotNullByDefault
public final class CucumberJavaStepDefinitionSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  public CucumberJavaStepDefinitionSearch() {
    super(true);
  }

  @Override
  public void processQuery(ReferencesSearch.SearchParameters queryParameters, Processor<? super PsiReference> consumer) {
    final PsiElement elementToSearch = queryParameters.getElementToSearch();
    final SearchScope searchScope = queryParameters.getEffectiveSearchScope();

    if (elementToSearch instanceof PsiMethod method) {
      final boolean isStepDefinition = CucumberJavaUtil.isAnnotationStepDefinition(method);
      if (!isStepDefinition) {
        return;
      }
      final List<PsiAnnotation> stepAnnotations = CucumberJavaUtil.getCucumberStepAnnotations(method);
      for (final PsiAnnotation stepAnnotation : stepAnnotations) {
        final String regexp = CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation);
        if (regexp == null) {
          continue;
        }
        CucumberUtil.findGherkinReferencesToElement(stepAnnotation, regexp, consumer, searchScope);
      }
    }

    if (elementToSearch instanceof PomTargetPsiElement pomTargetPsiElement) {
      if (pomTargetPsiElement.getTarget() instanceof CucumberJavaLambdaStepPomTarget pomTarget) {
        final String regexp = pomTarget.getName();
        if (regexp == null) {
          return;
        }
        final PsiElement methodCallExpression = pomTarget.getNavigationElement();
        if (!(methodCallExpression instanceof PsiMethodCallExpression)) {
          return;
        }
        CucumberUtil.findGherkinReferencesToElement(methodCallExpression, regexp, consumer, searchScope);
      }
    }
  }
}
