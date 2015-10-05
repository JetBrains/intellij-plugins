package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartVmServiceStackFrame extends XStackFrame {

  private final DartVmServiceDebugProcess myDebugProcess;
  private final String myIsolateId;

  public DartVmServiceStackFrame(@NotNull final DartVmServiceDebugProcess debugProcess,
                                 @NotNull final String isolateId) {
    myDebugProcess = debugProcess;
    myIsolateId = isolateId;
  }

  public String getIsolateId() {
    return myIsolateId;
  }

  @Nullable
  @Override
  public XSourcePosition getSourcePosition() {
    return super.getSourcePosition(); // todo
  }
}
