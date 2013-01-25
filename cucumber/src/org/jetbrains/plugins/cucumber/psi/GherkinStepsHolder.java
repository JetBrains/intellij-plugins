package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 * @date Aug 22, 2009
 */
public interface GherkinStepsHolder extends GherkinPsiElement, GherkinSuppressionHolder {
  GherkinStepsHolder[] EMPTY_ARRAY = new GherkinStepsHolder[0];

  String getScenarioName();

  GherkinStep[] getSteps();

  GherkinTag[] getTags();

  @Nullable
  String getScenarioTitle();
}
