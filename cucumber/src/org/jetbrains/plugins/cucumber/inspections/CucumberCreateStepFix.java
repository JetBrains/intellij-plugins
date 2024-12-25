// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.intention.HighPriorityAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;


public class CucumberCreateStepFix extends CucumberCreateStepFixBase implements HighPriorityAction {
  @Override
  public @NotNull String getName() {
    return CucumberBundle.message("cucumber.create.step.title");
  }

  @Override
  protected void createStepOrSteps(GherkinStep step, final @NotNull CucumberStepDefinitionCreationContext fileAndFrameworkType) {
    createFileOrStepDefinition(step, fileAndFrameworkType);
  }
}
