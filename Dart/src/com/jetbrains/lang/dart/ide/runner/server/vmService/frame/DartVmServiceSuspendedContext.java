package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.dartlang.vm.service.element.Frame;
import org.dartlang.vm.service.element.IsolateRef;
import org.jetbrains.annotations.NotNull;

public class DartVmServiceSuspendedContext extends XSuspendContext {
  private DartVmServiceDebugProcess myDebugProcess;
  @NotNull private final IsolateRef myIsolateRef;
  @NotNull private final Frame myTopFrame;

  public DartVmServiceSuspendedContext(@NotNull final DartVmServiceDebugProcess debugProcess,
                                       @NotNull final IsolateRef isolateRef,
                                       @NotNull final Frame topFrame) {
    myDebugProcess = debugProcess;
    myIsolateRef = isolateRef;
    myTopFrame = topFrame;
  }
}
