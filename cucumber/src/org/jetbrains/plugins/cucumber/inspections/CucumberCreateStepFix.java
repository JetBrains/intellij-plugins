package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

/**
 * @author yole
 */
public class CucumberCreateStepFix extends CucumberCreateStepFixBase implements HighPriorityAction {
  @NotNull
  public String getName() {
    return CucumberBundle.message("cucumber.create.step.title");
  }

  @Override
  protected void createStepOrSteps(GherkinStep step, PsiFile file) {
    createFileOrStepDefinition(step, file);
  }
}
