package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class CucumberCreateAllStepsFix extends CucumberCreateStepFixBase {
  @NotNull
  @Override
  public String getName() {
    return CucumberBundle.message("cucumber.create.all.steps.title");
  }

  @Override
  protected void createStepOrSteps(GherkinStep sourceStep, @Nullable final Pair<PsiFile, BDDFrameworkType> fileAndFrameworkType) {
    final PsiFile probableGherkinFile = sourceStep.getContainingFile();
    if (!(probableGherkinFile instanceof GherkinFile)) {
      return;
    }

    final Set<String> createdStepDefPatterns = new HashSet<>();
    final GherkinFile gherkinFile = (GherkinFile)probableGherkinFile;
    for (GherkinFeature feature : gherkinFile.getFeatures()) {
      for (GherkinStepsHolder stepsHolder : feature.getScenarios()) {
        for (GherkinStep step : stepsHolder.getSteps()) {
          final PsiReference[] references = step.getReferences();
          for (PsiReference reference : references) {
            if (!(reference instanceof CucumberStepReference)) continue;

            final AbstractStepDefinition definition = ((CucumberStepReference)reference).resolveToDefinition();
            if (definition == null) {
              String pattern = Pattern.quote(step.getStepName());
              pattern = StringUtil.trimEnd(StringUtil.trimStart(pattern, "\\Q"), "\\E");
              pattern = CucumberUtil.prepareStepRegexp(pattern);
              if (!createdStepDefPatterns.contains(pattern)) {
                createFileOrStepDefinition(step, fileAndFrameworkType);
                createdStepDefPatterns.add(pattern);
              }
            }
          }
        }
      }
    }
  }
}
