package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.dartlang.vm.service.element.Frame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartVmServiceStackFrame extends XStackFrame {

  private final DartVmServiceDebugProcess myDebugProcess;
  private final String myIsolateId;
  private final Frame myVmFrame;
  private final XSourcePosition mySourcePosition;

  public DartVmServiceStackFrame(@NotNull final DartVmServiceDebugProcess debugProcess,
                                 @NotNull final String isolateId,
                                 @NotNull final Frame vmFrame) {
    myDebugProcess = debugProcess;
    myIsolateId = isolateId;
    myVmFrame = vmFrame;
    mySourcePosition = debugProcess.getSourcePosition(isolateId, vmFrame.getLocation().getScript(), vmFrame.getLocation().getTokenPos());
  }

  public String getIsolateId() {
    return myIsolateId;
  }

  @Nullable
  @Override
  public XSourcePosition getSourcePosition() {
    return mySourcePosition;
  }

  // todo add method name
  //@Override
  //public void customizePresentation(@NotNull ColoredTextContainer component) {
  //}
}
