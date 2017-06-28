/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package com.jetbrains.lang.dart.ide.runner;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import org.jetbrains.annotations.NotNull;

public class DartExceptionBreakpointProperties extends XBreakpointProperties<DartExceptionBreakpointProperties> {
  private boolean myBreakOnAllExceptions = false;

  @NotNull
  @Override
  public DartExceptionBreakpointProperties getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull final DartExceptionBreakpointProperties state) {
    myBreakOnAllExceptions = state.myBreakOnAllExceptions;
  }

  public void setBreakOnAllExceptions(final boolean value) {
    myBreakOnAllExceptions = value;
  }

  public boolean isBreakOnAllExceptions() {
    return myBreakOnAllExceptions;
  }
}
