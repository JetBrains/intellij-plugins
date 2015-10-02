package com.jetbrains.lang.dart.ide.runner.server.vmService;

import org.dartlang.vm.service.VmServiceListener;
import org.dartlang.vm.service.element.Event;
import org.jetbrains.annotations.NotNull;

public class DartVmServiceListener implements VmServiceListener {
  private final DartVmServiceDebugProcess myDebugProcess;

  public DartVmServiceListener(@NotNull final DartVmServiceDebugProcess debugProcess) {
    myDebugProcess = debugProcess;
  }

  @Override
  public void received(@NotNull final String streamId, @NotNull final Event event) {
    switch (event.getKind()) {
      case IsolateStart:
        break;
      case IsolateRunnable:
        break;
      case IsolateExit:
        break;
      case IsolateUpdate:
        break;
      case PauseStart:
        myDebugProcess.getVmServiceWrapper().resumeIsolate(event.getIsolate());
        break;
      case PauseExit:
        break;
      case PauseBreakpoint:
        break;
      case PauseInterrupted:
        break;
      case PauseException:
        break;
      case Resume:
        break;
      case BreakpointAdded:
        break;
      case BreakpointResolved:
        break;
      case BreakpointRemoved:
        break;
      case GC:
        break;
      case WriteEvent:
        break;
    }
  }
}
