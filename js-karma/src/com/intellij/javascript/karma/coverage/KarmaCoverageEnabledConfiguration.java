package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.CoverageRunner;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;

import java.util.Objects;

public class KarmaCoverageEnabledConfiguration extends CoverageEnabledConfiguration {
  public KarmaCoverageEnabledConfiguration(RunConfigurationBase configuration) {
    super(configuration, Objects.requireNonNull(CoverageRunner.getInstance(KarmaCoverageRunner.class)));
  }
}
