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

public class CucumberJavaStepDefinitionSearch implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public boolean execute(@NotNull final ReferencesSearch.SearchParameters queryParameters,
                         @NotNull final Processor<? super PsiReference> consumer) {
    final PsiElement myElement = queryParameters.getElementToSearch();
    if (!(myElement instanceof PsiMethod)) {
      return true;
    }
    final PsiMethod method = (PsiMethod)myElement;
    Boolean isStepDefinition = ReadAction.compute(() -> CucumberJavaUtil.isStepDefinition(method));
    if (!isStepDefinition) {
      return true;
    }

    List<PsiAnnotation> stepAnnotations =
      ReadAction.compute(() -> CucumberJavaUtil.getCucumberStepAnnotations(method));

    for (PsiAnnotation stepAnnotation : stepAnnotations) {
      final String regexp = CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation);
      if (regexp == null) {
        continue;
      }
      boolean result = CucumberUtil.findGherkinReferencesToElement(myElement, regexp, consumer, queryParameters.getEffectiveSearchScope());
      if (!result) {
        return false;
      }
    }

    return true;
  }
}
