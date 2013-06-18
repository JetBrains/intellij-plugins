package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.CoverageRunner;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageEnabledConfiguration extends CoverageEnabledConfiguration {
  public KarmaCoverageEnabledConfiguration(KarmaRunConfiguration configuration) {
    super(configuration);
    KarmaCoverageRunner coverageRunner = CoverageRunner.getInstance(KarmaCoverageRunner.class);
    setCoverageRunner(coverageRunner);
  }
}
