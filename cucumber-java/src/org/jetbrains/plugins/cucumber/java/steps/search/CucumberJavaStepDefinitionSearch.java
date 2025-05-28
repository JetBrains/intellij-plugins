// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.List;

/// Handles [ReferencesSearch] requests made by [CucumberJavaMethodUsageSearcher].
public final class CucumberJavaStepDefinitionSearch implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public boolean execute(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
    final PsiElement elementToSearch = queryParameters.getElementToSearch();
    if (!(elementToSearch instanceof PsiMethod method)) return true;
    final boolean isStepDefinition = ReadAction.compute(() -> CucumberJavaUtil.isStepDefinition(method));
    if (!isStepDefinition) {
      return true;
    }
    final List<PsiAnnotation> stepAnnotations = ReadAction.compute(() -> CucumberJavaUtil.getCucumberStepAnnotations(method));
    for (final PsiAnnotation stepAnnotation : stepAnnotations) {
      final String regexp = CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation);
      if (regexp == null) {
        continue;
      }
      var found = CucumberUtil.findGherkinReferencesToElement(elementToSearch, regexp, consumer, queryParameters.getEffectiveSearchScope());
      if (!found) {
        return false;
      }
    }

    return true;
  }
}
