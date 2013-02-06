package org.jetbrains.plugins.cucumber.java.steps;

import org.jetbrains.plugins.cucumber.java.config.CucumberConfigUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

public class CucumberVersionProvider {
  public String getVersion(GherkinStep step) {
    return CucumberConfigUtil.getCucumberCoreVersion(step);
  }
}