/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.coverage;

import com.intellij.coverage.CoverageRunner;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.process.ProcessHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DartCoverageEnabledConfiguration extends CoverageEnabledConfiguration {
  @Nullable private ProcessHandler coverageProcess;

  public DartCoverageEnabledConfiguration(RunConfigurationBase configuration) {
    super(configuration, Objects.requireNonNull(CoverageRunner.getInstance(DartCoverageRunner.class)));
  }

  @Nullable
  public ProcessHandler getCoverageProcess() {
    return coverageProcess;
  }

  public void setCoverageProcess(@Nullable ProcessHandler coverageProcess) {
    this.coverageProcess = coverageProcess;
  }
}
