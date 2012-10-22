package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;

/**
 * User: Andrey.Vokin
 * Date: 10/22/12
 */
public class CucumberJavaScenarioRunConfigurationProducer extends CucumberJavaFeatureRunConfigurationProducer {
  private GherkinStepsHolder scenario;

  @Override
  protected void processConfiguration(@NotNull final CucumberJavaRunConfiguration configuration) {
    final String programParameters = configuration.getProgramParameters();
    final GherkinStepsHolder scenario = PsiTreeUtil.getParentOfType(mySourceElement, GherkinScenario.class, GherkinScenarioOutline.class);
    if (scenario != null) {
      configuration.setProgramParameters(programParameters + " --name \"" + scenario.getScenarioName() + "\"");
    }
  }

  @Override
  protected String getName() {
    return "Scenario: " + scenario.getScenarioName();
  }

  @Override
  protected boolean isApplicable(PsiElement locationElement, Module module) {
    scenario = PsiTreeUtil.getParentOfType(mySourceElement, GherkinScenario.class, GherkinScenarioOutline.class);
    return scenario != null;
  }
}
