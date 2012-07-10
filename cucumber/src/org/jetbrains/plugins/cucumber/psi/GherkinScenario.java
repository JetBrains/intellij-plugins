package org.jetbrains.plugins.cucumber.psi;

/**
 * @author yole
 */
public interface GherkinScenario extends GherkinStepsHolder {
  boolean isBackground();
}
