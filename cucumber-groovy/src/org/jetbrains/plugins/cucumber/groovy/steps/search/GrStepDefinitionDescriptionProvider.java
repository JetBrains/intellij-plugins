// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageViewNodeTextLocation;
import com.intellij.usageView.UsageViewTypeLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

final class GrStepDefinitionDescriptionProvider implements ElementDescriptionProvider {
  @Override
  public @Nullable String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
    if (location instanceof UsageViewNodeTextLocation || location instanceof UsageViewTypeLocation) {
      if (GrCucumberUtil.isStepDefinition(element) || element instanceof GrReferenceExpression && GrCucumberUtil.isStepDefinition(element.getParent())) {
        return CucumberBundle.message("step.definition") ;
      }
    }
    return null;
  }
}
