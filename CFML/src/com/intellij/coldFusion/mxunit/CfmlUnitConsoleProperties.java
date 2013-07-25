package com.intellij.coldFusion.mxunit;

import com.intellij.execution.Executor;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;

public class CfmlUnitConsoleProperties extends SMTRunnerConsoleProperties {
  public CfmlUnitConsoleProperties(final CfmlUnitRunConfiguration config, Executor executor) {
    super(new RuntimeConfigurationProducer.DelegatingRuntimeConfiguration<CfmlUnitRunConfiguration>(config), "CfmlUnit", executor);
  }

  @Override
  public boolean isDebug() {
    return false;
  }

  @Override
  public boolean isPaused() {
    return false;
  }
}
