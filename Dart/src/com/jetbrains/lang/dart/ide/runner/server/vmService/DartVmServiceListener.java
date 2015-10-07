package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceSuspendedContext;
import org.dartlang.vm.service.VmServiceListener;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.NotNull;

public class DartVmServiceListener implements VmServiceListener {
  private static final Logger LOG = Logger.getInstance(DartVmServiceListener.class.getName());

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
        myDebugProcess.isolateExit(event.getIsolate());
        break;
      case IsolateUpdate:
        break;
      case PauseStart:
        myDebugProcess.getVmServiceWrapper().handleIsolatePausedOnStart(event.getIsolate());
        break;
      case PauseExit:
        break;
      case PauseBreakpoint:
        myDebugProcess.isolateSuspended(event.getIsolate());

        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
          @Override
          public void run() {
            onPauseBreakpoint(event.getIsolate(), event.getPauseBreakpoints(), event.getTopFrame());
          }
        });
        break;
      case PauseInterrupted:
        myDebugProcess.isolateSuspended(event.getIsolate());
        break;
      case PauseException:
        myDebugProcess.isolateSuspended(event.getIsolate());
        break;
      case Resume:
        myDebugProcess.isolateResumed(event.getIsolate());
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

  private void onPauseBreakpoint(@NotNull final IsolateRef isolateRef,
                                 @NotNull final ElementList<Breakpoint> vmBreakpoints,
                                 @NotNull final Frame topFrame) {
    if (vmBreakpoints.isEmpty()) {
      myDebugProcess.getSession().positionReached(new DartVmServiceSuspendedContext(myDebugProcess, isolateRef, topFrame));
    }
    else {
      if (vmBreakpoints.size() > 1) {
        // Shouldn't happen. IDE doesn't allow to set 2 breakpoints on one line.
        LOG.error(vmBreakpoints.size() + " breakpoints hit in one shot.");
      }

      final XLineBreakpoint<XBreakpointProperties> xBreakpoint = myBreakpointHandler.getXBreakpoint(vmBreakpoints.get(0));
      myDebugProcess.getSession()
        .breakpointReached(xBreakpoint, null, new DartVmServiceSuspendedContext(myDebugProcess, isolateRef, topFrame));
    }
  }
}
