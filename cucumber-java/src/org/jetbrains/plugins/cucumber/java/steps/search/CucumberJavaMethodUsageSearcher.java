// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.util.List;

public class CucumberJavaMethodUsageSearcher extends QueryExecutorBase<PsiReference, MethodReferencesSearch.SearchParameters> {
  public CucumberJavaMethodUsageSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull final MethodReferencesSearch.SearchParameters p,
                           @NotNull final Processor<? super PsiReference> consumer) {
    SearchScope scope = p.getEffectiveSearchScope();
    if (!(scope instanceof GlobalSearchScope)) {
      return;
    }

    final PsiMethod method = p.getMethod();

    final GlobalSearchScope restrictedScope = GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)scope,
                                                                                              GherkinFileType.INSTANCE);
    List<PsiAnnotation> stepAnnotations = CucumberJavaUtil.getCucumberStepAnnotations(method);
    for (PsiAnnotation stepAnnotation : stepAnnotations) {
      final String regexp = stepAnnotation != null ? CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation) : null;
      if (regexp == null) {
        continue;
      }
      ReferencesSearch.search(new ReferencesSearch.SearchParameters(method, restrictedScope, false, p.getOptimizer())).forEach(consumer);
    }
  }
}
