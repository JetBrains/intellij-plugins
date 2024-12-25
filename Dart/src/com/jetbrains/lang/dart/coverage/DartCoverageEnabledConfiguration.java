// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.coverage;

import com.intellij.coverage.CoverageRunner;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.process.ProcessHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DartCoverageEnabledConfiguration extends CoverageEnabledConfiguration {
  private @Nullable ProcessHandler coverageProcess;

  public DartCoverageEnabledConfiguration(RunConfigurationBase configuration) {
    super(configuration, Objects.requireNonNull(CoverageRunner.getInstance(DartCoverageRunner.class)));
  }

  public @Nullable ProcessHandler getCoverageProcess() {
    return coverageProcess;
  }

  public void setCoverageProcess(@Nullable ProcessHandler coverageProcess) {
    this.coverageProcess = coverageProcess;
  }
}
