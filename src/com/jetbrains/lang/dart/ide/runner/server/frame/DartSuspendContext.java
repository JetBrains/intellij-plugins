package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;

import java.util.List;

public class DartSuspendContext extends XSuspendContext {
  private final DartExecutionStack myExecutionStack;

  public DartSuspendContext(DartCommandLineDebugProcess debugProcess) {
    myExecutionStack = new DartExecutionStack(debugProcess);
  }

  public DartSuspendContext(DartCommandLineDebugProcess debugProcess, List<DartStackFrame> frames) {
    myExecutionStack = new DartExecutionStack(debugProcess, frames);
  }

  @Override
  public XExecutionStack getActiveExecutionStack() {
    return myExecutionStack;
  }
}
