package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 * @date Aug 22, 2009
 */
public interface GherkinStepsHolder extends GherkinPsiElement, GherkinSuppressionHolder {
  GherkinStepsHolder[] EMPTY_ARRAY = new GherkinStepsHolder[0];

  @NotNull
  String getScenarioName();

  @NotNull
  GherkinStep[] getSteps();

  GherkinTag[] getTags();

  @NotNull
  String getScenarioKeyword();
}
