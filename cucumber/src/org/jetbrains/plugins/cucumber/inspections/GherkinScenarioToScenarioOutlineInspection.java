// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinElementFactory;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordTable;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline;
import org.jetbrains.plugins.cucumber.psi.GherkinUtil;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.util.Collection;

import static org.jetbrains.plugins.cucumber.psi.GherkinElementTypes.EXAMPLES_BLOCK;

public final class GherkinScenarioToScenarioOutlineInspection extends GherkinInspection {

  private static final class Holder {
    static final LocalQuickFix CONVERT_SCENARIO_TO_OUTLINE_FIX = new ConvertScenarioToOutlineFix();
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                                 boolean isOnTheFly,
                                                 @NotNull LocalInspectionToolSession session) {
    return new GherkinElementVisitor() {
      @Override
      public void visitScenario(GherkinScenario scenario) {
        if (scenario instanceof GherkinScenarioOutline) {
          return;
        }

        if (ContainerUtil.or(scenario.getChildren(), p -> PsiUtilCore.getElementType(p) == EXAMPLES_BLOCK)) {
          holder.registerProblem(scenario, scenario.getFirstChild().getTextRangeInParent(),
                                 CucumberBundle.message("inspection.gherkin.scenario.with.examples.section.error.message"),
                                 Holder.CONVERT_SCENARIO_TO_OUTLINE_FIX);
        }
      }
    };
  }

  private static class ConvertScenarioToOutlineFix implements LocalQuickFix {
    @Override
    public @NotNull String getFamilyName() {
      return CucumberBundle.message("inspection.gherkin.scenario.with.examples.section.quickfix.name");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      GherkinScenario scenario = (GherkinScenario)descriptor.getPsiElement();
      String language = GherkinUtil.getFeatureLanguage((GherkinFile)scenario.getContainingFile());

      GherkinKeywordTable keywordsTable = JsonGherkinKeywordProvider.getKeywordProvider().getKeywordsTable(language);
      Collection<String> scenarioKeywords = keywordsTable.getScenarioKeywords();
      String scenarioRegexp = StringUtil.join(scenarioKeywords, "|");
      String scenarioOutlineKeyword = keywordsTable.getScenarioOutlineKeyword();

      String scenarioOutlineText = scenario.getText().replaceFirst(scenarioRegexp, scenarioOutlineKeyword);

      GherkinScenarioOutline scenarioOutline =
        (GherkinScenarioOutline)GherkinElementFactory.createScenarioFromText(project, language, scenarioOutlineText);
      scenario.replace(scenarioOutline);
    }
  }
}
