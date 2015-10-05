package com.jetbrains.lang.dart.ide.runner.server.vmService;

import org.dartlang.vm.service.VmServiceListener;
import org.dartlang.vm.service.element.Event;
import org.jetbrains.annotations.NotNull;

public class DartVmServiceListener implements VmServiceListener {
  private final DartVmServiceDebugProcess myDebugProcess;
  private DartVmServiceBreakpointHandler myBreakpointHandler;

  public DartVmServiceListener(@NotNull final DartVmServiceDebugProcess debugProcess,
                               @NotNull final DartVmServiceBreakpointHandler breakpointHandler) {
    myDebugProcess = debugProcess;
    myBreakpointHandler = breakpointHandler;
  }

  @Override
  public void received(@NotNull final String streamId, @NotNull final Event event) {
    switch (event.getKind()) {
      case IsolateStart:
        break;
      case IsolateRunnable:
        break;
      case IsolateExit:
        myDebugProcess.getVmServiceWrapper().handleIsolateExit(event.getIsolate());
        break;
      case IsolateUpdate:
        break;
      case PauseStart:
        myDebugProcess.getVmServiceWrapper().handleIsolatePausedOnStart(event.getIsolate());
        break;
      case PauseExit:
        break;
      case PauseBreakpoint:
        myDebugProcess.getVmServiceWrapper().resumeIsolate(event.getIsolate().getId());
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
        myBreakpointHandler.breakpointResolved(event.getBreakpoint());
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
