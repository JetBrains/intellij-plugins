// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.intention.HighPriorityAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;


public class CucumberCreateStepFix extends CucumberCreateStepFixBase implements HighPriorityAction {
  @Override
  @NotNull
  public String getName() {
    return CucumberBundle.message("cucumber.create.step.title");
  }

  @Override
  protected void createStepOrSteps(GherkinStep step, @NotNull final CucumberStepDefinitionCreationContext fileAndFrameworkType) {
    createFileOrStepDefinition(step, fileAndFrameworkType);
  }
}
