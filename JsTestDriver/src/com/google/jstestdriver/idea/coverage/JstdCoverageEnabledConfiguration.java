package com.google.jstestdriver.idea.coverage;

import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.intellij.coverage.CoverageRunner;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;

/**
 * @author Sergey Simonchik
 */
public class JstdCoverageEnabledConfiguration extends CoverageEnabledConfiguration {
  public JstdCoverageEnabledConfiguration(JstdRunConfiguration configuration) {
    super(configuration);
    JstdCoverageRunner coverageRunner = CoverageRunner.getInstance(JstdCoverageRunner.class);
    setCoverageRunner(coverageRunner);
  }
}
