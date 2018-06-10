package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.CoverageRunner;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageEnabledConfiguration extends CoverageEnabledConfiguration {
  public KarmaCoverageEnabledConfiguration(RunConfigurationBase configuration) {
    super(configuration);
    KarmaCoverageRunner coverageRunner = CoverageRunner.getInstance(KarmaCoverageRunner.class);
    setCoverageRunner(coverageRunner);
  }
}
