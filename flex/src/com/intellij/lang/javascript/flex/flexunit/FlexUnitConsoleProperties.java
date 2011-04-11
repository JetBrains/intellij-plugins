package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RuntimeConfiguration;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.util.config.Storage;

public class FlexUnitConsoleProperties extends TestConsoleProperties {
  private final RuntimeConfiguration myConfig;

  public FlexUnitConsoleProperties(final FlexUnitRunConfiguration config, Executor executor) {
    super(new Storage.PropertiesComponentStorage("FlexUnitSupport.", PropertiesComponent.getInstance()), config.getProject(), executor);
    myConfig = new RuntimeConfigurationProducer.DelegatingRuntimeConfiguration<FlexUnitRunConfiguration>(config);
  }

  public RuntimeConfiguration getConfiguration() {
    return myConfig;
  }
}
