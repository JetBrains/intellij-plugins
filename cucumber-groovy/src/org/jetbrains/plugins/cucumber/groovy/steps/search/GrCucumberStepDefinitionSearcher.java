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
package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

/**
 * @author Max Medvedev
 */
public class GrCucumberStepDefinitionSearcher implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public boolean execute(@NotNull final ReferencesSearch.SearchParameters queryParameters,
                         @NotNull final Processor<? super PsiReference> consumer) {
    return ApplicationManager.getApplication().runReadAction((NullableComputable<Boolean>)() -> {
      PsiElement element = getStepDefinition(queryParameters.getElementToSearch());
      if (element == null) return true;

      String regexp = GrCucumberUtil.getStepDefinitionPatternText((GrMethodCall)element);
      if (regexp == null) return true;

      return CucumberUtil.findGherkinReferencesToElement(element, regexp, consumer, queryParameters.getEffectiveSearchScope());
    });
  }

  public static PsiElement getStepDefinition(final PsiElement element) {
    if (GrCucumberUtil.isStepDefinition(element)) {
      return element;
    }

    if (element instanceof PomTargetPsiElement) {
      final PomTarget target = ((PomTargetPsiElement)element).getTarget();
      if (target instanceof GrStepDefinition) {
        return ((GrStepDefinition)target).getElement();
      }
    }

    return null;
  }
}
