// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import org.jetbrains.annotations.NotNull;

public class DartExceptionBreakpointProperties extends XBreakpointProperties<DartExceptionBreakpointProperties> {
  private boolean myBreakOnAllExceptions = false;

  @Override
  public @NotNull DartExceptionBreakpointProperties getState() {
    return this;
  }

  @Override
  public void loadState(final @NotNull DartExceptionBreakpointProperties state) {
    myBreakOnAllExceptions = state.myBreakOnAllExceptions;
  }

  public void setBreakOnAllExceptions(final boolean value) {
    myBreakOnAllExceptions = value;
  }

  public boolean isBreakOnAllExceptions() {
    return myBreakOnAllExceptions;
  }
}
