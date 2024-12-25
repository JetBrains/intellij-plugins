// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;

public final class GherkinMisplacedBackgroundInspection extends GherkinInspection {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitScenario(GherkinScenario scenario) {
        if (scenario.isBackground()) {

          PsiElement element = scenario.getPrevSibling();

          while (element != null) {
            if (element instanceof GherkinScenario) {
              if (!((GherkinScenario)element).isBackground()) {
                holder.registerProblem(scenario.getFirstChild(), CucumberBundle.message("inspection.gherkin.background.after.scenario.error.message"), ProblemHighlightType.ERROR);
                break;
              }
            }
            element = element.getPrevSibling();
          }
        }
      }
    };
  }

  @Override
  public @NotNull String getShortName() {
    return "GherkinMisplacedBackground";
  }
}
