// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;


public final class CucumberMissedExamplesInspection extends GherkinInspection {
  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Override
  public @NotNull String getShortName() {
    return "CucumberMissedExamples";
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      // A scenario with Examples is an outline and never visited here. Any other scenario - whatever its
      // keyword - needs Examples only when a step uses a <placeholder> that nothing would fill.
      @Override
      public void visitScenario(GherkinScenario scenario) {
        super.visitScenario(scenario);
        if (scenario.isBackground()) {
          return;
        }
        boolean usesPlaceholder = false;
        for (GherkinStep step : scenario.getSteps()) {
          if (!step.getParamsSubstitutions().isEmpty()) {
            usesPlaceholder = true;
            break;
          }
        }
        if (!usesPlaceholder) {
          return;
        }
        ASTNode[] keyword = scenario.getNode().getChildren(GherkinTokenTypes.KEYWORDS);
        if (keyword.length > 0) {
          holder.registerProblem(scenario, keyword[0].getTextRange().shiftRight(-scenario.getTextOffset()),
                                 CucumberBundle.message("inspection.missed.example.msg.name"), new CucumberCreateExamplesSectionFix());
        }
      }
    };
  }

  @Override
  public @NotNull HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}