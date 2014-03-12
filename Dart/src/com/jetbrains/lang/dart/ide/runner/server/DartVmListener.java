package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.jetbrains.lang.dart.ide.runner.server.frame.DartSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.google.*;

import java.io.IOException;
import java.util.List;

import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.LOG;

// see com.google.dart.tools.debug.core.server.ServerDebugTarget
public class DartVmListener implements VmListener {
  private final DartCommandLineDebugProcess myDebugProcess;
  private boolean myFirstBreak = true;

  public DartVmListener(final DartCommandLineDebugProcess debugProcess) {
    myDebugProcess = debugProcess;
  }

  public void connectionOpened(final VmConnection connection) {
    LOG.debug("connection opened");
    myDebugProcess.setVmConnected(true);
  }

  public void connectionClosed(final VmConnection connection) {
    LOG.debug("connection closed");
    myDebugProcess.setVmConnected(false);
    connection.handleTerminated();
  }

  public void isolateCreated(final VmIsolate isolate) {
    LOG.debug("isolate created: " + isolate.getId());
    myDebugProcess.isolateCreated(isolate);
  }

  public void isolateShutdown(final VmIsolate isolate) {
    LOG.debug("isolate shutdown: " + isolate.getId());
  }

  public void breakpointResolved(final VmIsolate isolate, final VmBreakpoint breakpoint) {
    LOG.debug("breakpoint resolved: " + breakpoint.getBreakpointId());
    myDebugProcess.getDartBreakpointsHandler().breakpointResolved(breakpoint);
  }

  public void debuggerPaused(final PausedReason reason, final VmIsolate isolate, final List<VmCallFrame> frames, final VmValue exception) {
    LOG.debug("debugger paused, reason: " + reason.name());

    // todo handle exception
    boolean resumed = false;

    if (myFirstBreak) {
      myFirstBreak = false;

      // init everything
      firstIsolateInit(isolate);

      if (PausedReason.breakpoint == reason) {
        // If this is our first break, and there is no user breakpoint here, and the stop is on the main() method, then resume.
        if (frames.size() > 0 &&
            frames.get(0).isMain() &&
            !myDebugProcess.getDartBreakpointsHandler().hasInitialBreakpointHere(frames.get(0).getLocation())) {
          resumed = true;

          try {
            myDebugProcess.getVmConnection().resume(isolate);
          }
          catch (IOException ioe) {
            LOG.error(ioe);
          }
        }
      }
    }

    if (!resumed) {
      /*
      ServerDebugThread thread = findThread(isolate);

      if (thread != null) {
        if (exception != null) {
          printExceptionToStdout(exception);
        }

        thread.handleDebuggerPaused(reason, frames, exception);
      }
      */

      myDebugProcess.getSession().positionReached(new DartSuspendContext(myDebugProcess, frames));
    }
  }

  private void firstIsolateInit(final VmIsolate isolate) {
    myDebugProcess.getDartBreakpointsHandler().registerInitialBreakpoints();

    try {
      myDebugProcess.getVmConnection().enableAllSteppingSync(isolate);
    }
    catch (IOException e) {
      LOG.error(e);
    }

    try {
      myDebugProcess.getVmConnection()
        .setPauseOnExceptionSync(isolate, VmConnection.BreakOnExceptionsType.unhandled); // todo add an option for BreakOnExceptionsType
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

  public void debuggerResumed(final VmIsolate isolate) {
    LOG.debug("debugger resumed: " + isolate.getId());
  }
}
