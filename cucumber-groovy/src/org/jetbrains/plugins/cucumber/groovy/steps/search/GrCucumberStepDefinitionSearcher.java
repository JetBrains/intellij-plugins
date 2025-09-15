// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

/**
 * @author Max Medvedev
 */
public final class GrCucumberStepDefinitionSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  public GrCucumberStepDefinitionSearcher() {
    super(true);
  }

  @Override
  public void processQuery(final @NotNull ReferencesSearch.SearchParameters queryParameters,
                           final @NotNull Processor<? super PsiReference> consumer) {

    PsiElement element = getStepDefinition(queryParameters.getElementToSearch());
    if (element == null) return;

    String regexp = GrCucumberUtil.getStepDefinitionPatternText((GrMethodCall)element);
    if (regexp == null) return;

    CucumberUtil.findGherkinReferencesToElement(element, regexp, consumer, queryParameters.getEffectiveSearchScope());
  }

  public static PsiElement getStepDefinition(final PsiElement element) {
    if (GrCucumberUtil.isStepDefinition(element)) {
      return element;
    }

    if (element instanceof PomTargetPsiElement psiElement) {
      final PomTarget target = psiElement.getTarget();
      if (target instanceof GrStepDefinition definition) {
        return definition.getElement();
      }
    }

    return null;
  }
}
