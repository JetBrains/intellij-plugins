// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNullByDefault;

/// Searches for Gherkin references to Cucumber step definition methods via [MethodReferencesSearch].
///
/// This complements [CucumberJavaStepDefinitionSearch] (registered for `referencesSearch`)
/// by ensuring that Gherkin references are also found when the search goes through `methodReferencesSearch`,
/// which is used by the unused symbol detection in `UnusedSymbolUtil.processUsages()`.
@NotNullByDefault
public final class CucumberJavaMethodReferenceSearch extends QueryExecutorBase<PsiReference, MethodReferencesSearch.SearchParameters> {
  public CucumberJavaMethodReferenceSearch() {
    super(true);
  }

  @Override
  public void processQuery(MethodReferencesSearch.SearchParameters queryParameters, Processor<? super PsiReference> consumer) {
    CucumberJavaSearchUtil.findGherkinReferencesToMethod(consumer, queryParameters.getEffectiveSearchScope(), queryParameters.getMethod());
  }
}
