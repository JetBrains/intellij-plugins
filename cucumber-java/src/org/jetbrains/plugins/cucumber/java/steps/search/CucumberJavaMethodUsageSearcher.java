// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.util.List;

/// Provides 'Find Usages' for Java methods that implement Cucumber step definitions (aka "glue").
///
/// For each Java step definition method, it performs a [ReferencesSearch] for that method across all Gherkin feature files.
///
/// @see CucumberJavaStepDefinitionSearch
public final class CucumberJavaMethodUsageSearcher extends QueryExecutorBase<PsiReference, MethodReferencesSearch.SearchParameters> {
  public CucumberJavaMethodUsageSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull MethodReferencesSearch.SearchParameters queryParameters,
                           @NotNull Processor<? super PsiReference> consumer) {
    final SearchScope scope = queryParameters.getEffectiveSearchScope();
    if (!(scope instanceof GlobalSearchScope globalSearchScope)) return;

    final PsiMethod method = queryParameters.getMethod();

    final GlobalSearchScope restrictedScope = GlobalSearchScope.getScopeRestrictedByFileTypes(globalSearchScope, GherkinFileType.INSTANCE);
    final List<PsiAnnotation> stepAnnotations = CucumberJavaUtil.getCucumberStepAnnotations(method);
    for (final PsiAnnotation stepAnnotation : stepAnnotations) {
      final String regexp = stepAnnotation != null ? CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation) : null;
      if (regexp == null) continue;
      final var searchParameters = new ReferencesSearch.SearchParameters(method, restrictedScope, false, queryParameters.getOptimizer());
      ReferencesSearch
        .search(searchParameters)
        .forEach(consumer);
    }
  }
}
