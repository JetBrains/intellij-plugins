/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion.mxunit;

import com.intellij.execution.Executor;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import org.jetbrains.annotations.NotNull;

public class CfmlUnitConsoleProperties extends SMTRunnerConsoleProperties {
  public CfmlUnitConsoleProperties(@NotNull CfmlUnitRunConfiguration config, @NotNull Executor executor) {
    super(config, "CfmlUnit", executor);
  }

  @Override
  public boolean isDebug() {
    return false;
  }

  @Override
  public boolean isPaused() {
    return false;
  }

  @Override
  public SMTestLocator getTestLocator() {
    return CfmlUnitQualifiedNameLocationProvider.INSTANCE;
  }
}
