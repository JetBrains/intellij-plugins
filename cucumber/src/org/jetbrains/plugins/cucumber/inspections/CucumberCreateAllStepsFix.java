package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

/**
 * User: Andrey.Vokin
 * Date: 10/8/2014.
 */
public class CucumberCreateAllStepsFix extends CucumberCreateStepFixBase {
  @NotNull
  @Override
  public String getName() {
    return CucumberBundle.message("cucumber.create.all.steps.title");
  }

  @Override
  protected void createStepOrSteps(GherkinStep sourceStep, PsiFile file) {
    final PsiFile probableGherkinFile = sourceStep.getContainingFile();
    if (!(probableGherkinFile instanceof GherkinFile)) {
      return;
    }

    final GherkinFile gherkinFile = (GherkinFile)probableGherkinFile;
    for (GherkinFeature feature : gherkinFile.getFeatures()) {
      for (GherkinStepsHolder stepsHolder : feature.getScenarios()) {
        for (GherkinStep step : stepsHolder.getSteps()) {
          final PsiReference[] references = step.getReferences();
          for (PsiReference reference : references) {
            if (!(reference instanceof CucumberStepReference)) continue;

            final AbstractStepDefinition definition = ((CucumberStepReference)reference).resolveToDefinition();
            if (definition == null) {
              createFileOrStepDefinition(step, file);
            }
          }
        }
      }
    }
  }
}
