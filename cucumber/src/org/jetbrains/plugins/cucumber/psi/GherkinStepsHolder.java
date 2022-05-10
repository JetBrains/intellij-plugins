package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public interface GherkinStepsHolder extends GherkinPsiElement, GherkinSuppressionHolder {
  GherkinStepsHolder[] EMPTY_ARRAY = new GherkinStepsHolder[0];

  @NotNull
  String getScenarioName();

  GherkinStep @NotNull [] getSteps();

  GherkinTag[] getTags();

  @NotNull
  String getScenarioKeyword();
}
