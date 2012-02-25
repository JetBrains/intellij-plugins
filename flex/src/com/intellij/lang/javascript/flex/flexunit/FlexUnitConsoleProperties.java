package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.Executor;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;

public class FlexUnitConsoleProperties extends SMTRunnerConsoleProperties {

  public FlexUnitConsoleProperties(final FlexUnitRunConfiguration config, Executor executor) {
    super(new RuntimeConfigurationProducer.DelegatingRuntimeConfiguration<FlexUnitRunConfiguration>(config),
          "FlexUnit", executor);
  }
}
