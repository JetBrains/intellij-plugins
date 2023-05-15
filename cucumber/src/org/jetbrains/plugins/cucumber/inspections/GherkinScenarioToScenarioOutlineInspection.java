// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.util.Collection;
import java.util.stream.Stream;

import static org.jetbrains.plugins.cucumber.psi.GherkinElementTypes.EXAMPLES_BLOCK;

public class GherkinScenarioToScenarioOutlineInspection extends GherkinInspection {

  private static final class Holder {
    static final LocalQuickFix CONVERT_SCENARIO_TO_OUTLINE_FIX = new ConvertScenarioToOutlineFix();
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                        boolean isOnTheFly,
                                        @NotNull LocalInspectionToolSession session) {
    return new GherkinElementVisitor() {
      @Override
      public void visitScenario(GherkinScenario scenario) {
        if (scenario instanceof GherkinScenarioOutline) {
          return;
        }

        if (Stream.of(scenario.getChildren()).anyMatch(p -> PsiUtilCore.getElementType(p) == EXAMPLES_BLOCK)) {
          holder.registerProblem(scenario, scenario.getFirstChild().getTextRangeInParent(),
                                 CucumberBundle.message("inspection.gherkin.scenario.with.examples.section.error.message"),
                                 Holder.CONVERT_SCENARIO_TO_OUTLINE_FIX);
        }
      }
    };
  }

  private static class ConvertScenarioToOutlineFix implements LocalQuickFix {
    @Override
    @NotNull
    public String getFamilyName() {
      return CucumberBundle.message("inspection.gherkin.scenario.with.examples.section.quickfix.name");
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
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
