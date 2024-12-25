// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;

public class CucumberJavaScenarioRunConfigurationProducer extends CucumberJavaFeatureRunConfigurationProducer {
  private static final String SCENARIO_OUTLINE_PARAMETER_REGEXP = "\\\\<.*?\\\\>";
  private static final String ANY_STRING_REGEXP = ".*";
  private static final String NAME_FILTER_TEMPLATE = "^%s$";

  @Override
  protected String getNameFilter(@NotNull ConfigurationContext context) {
    final PsiElement sourceElement = context.getPsiLocation();

    final GherkinStepsHolder scenario = PsiTreeUtil.getParentOfType(sourceElement, GherkinScenario.class, GherkinScenarioOutline.class);
    if (scenario != null) {
      String nameFilter = String.format(NAME_FILTER_TEMPLATE, StringUtil.escapeToRegexp(scenario.getScenarioName()));
      if (scenario instanceof GherkinScenarioOutline) {
        nameFilter = nameFilter.replaceAll(SCENARIO_OUTLINE_PARAMETER_REGEXP, ANY_STRING_REGEXP);
      }

      return nameFilter;
    }

    return super.getNameFilter(context);
  }

  @Override
  protected @Nullable VirtualFile getFileToRun(ConfigurationContext context) {
    final PsiElement element = context.getPsiLocation();
    final GherkinStepsHolder scenario = PsiTreeUtil.getParentOfType(element, GherkinScenario.class, GherkinScenarioOutline.class);
    final PsiFile psiFile = scenario != null ? scenario.getContainingFile() : null;
    return psiFile != null ? psiFile.getVirtualFile() : null;
  }

  @Override
  protected String getConfigurationName(final @NotNull ConfigurationContext context) {
    final PsiElement sourceElement = context.getPsiLocation();
    final GherkinStepsHolder scenario = PsiTreeUtil.getParentOfType(sourceElement, GherkinScenario.class, GherkinScenarioOutline.class);

    return "Scenario: " + (scenario != null ? scenario.getScenarioName() : "");
  }
}
