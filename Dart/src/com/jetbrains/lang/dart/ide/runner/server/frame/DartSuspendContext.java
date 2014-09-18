package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallFrame;
import com.jetbrains.lang.dart.ide.runner.server.google.VmValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartSuspendContext extends XSuspendContext {
  private final @NotNull DartExecutionStack myExecutionStack;

  public DartSuspendContext(@NotNull final DartCommandLineDebugProcess debugProcess,
                            @NotNull final List<VmCallFrame> vmCallFrames,
                            @Nullable final VmValue exception) {
    myExecutionStack = new DartExecutionStack(debugProcess, vmCallFrames, exception);
  }

  @Override
  @NotNull
  public XExecutionStack getActiveExecutionStack() {
    return myExecutionStack;
  }
}
