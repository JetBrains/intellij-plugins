package org.jetbrains.plugins.cucumber.java.steps;

import org.jetbrains.plugins.cucumber.psi.GherkinStep;

public class ConstantCucumberVersion extends CucumberVersionProvider {

  public static CucumberVersionProvider cucumber_1_1() {
    return new ConstantCucumberVersion("1.1");
  }

  public static CucumberVersionProvider cucumber_1_0() {
    return new ConstantCucumberVersion("1.0");
  }

  private String version;

  public ConstantCucumberVersion(String version) {
    this.version = version;
  }

  @Override
  public String getVersion(GherkinStep step) {
    return version;
  }
}
