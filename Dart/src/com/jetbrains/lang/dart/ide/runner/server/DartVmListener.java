package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.ide.runner.server.frame.DartSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.google.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    myDebugProcess.getSession().stop();
  }

  public void isolateCreated(final VmIsolate isolate) {
    LOG.debug("isolate created: " + isolate.getId());
  }

  public void isolateShutdown(final VmIsolate isolate) {
    LOG.debug("isolate shutdown: " + isolate.getId());
  }

  public void breakpointResolved(final VmIsolate isolate, final VmBreakpoint breakpoint) {
    LOG.debug("breakpoint resolved: " + breakpoint.getBreakpointId());
    myDebugProcess.getDartBreakpointsHandler().breakpointResolved(breakpoint);
  }

  public void debuggerPaused(final PausedReason reason,
                             final VmIsolate isolate,
                             final List<VmCallFrame> frames,
                             final VmValue exception,
                             final boolean isStepping) {
    LOG.debug("debugger paused, reason: " + reason.name());
    final VmCallFrame topFrame = frames.isEmpty() ? null : frames.get(0);

    // todo handle exception
    if (myFirstBreak) {
      myFirstBreak = false;

      // init everything
      myDebugProcess.setMainIsolate(isolate);
      firstIsolateInit(isolate);

      if (PausedReason.breakpoint == reason) {
        // If this is our first break, and there is no user breakpoint here, and the stop is on the main() method, then resume.
        if (topFrame != null && topFrame.isMain() &&
            !myDebugProcess.getDartBreakpointsHandler().hasInitialBreakpointHere(frames.get(0).getLocation())) {
          resume(isolate);
          return;
        }
      }
    }

      /*
      ServerDebugThread thread = findThread(isolate);

      if (thread != null) {
        if (exception != null) {
          printExceptionToStdout(exception);
        }

        thread.handleDebuggerPaused(reason, frames, exception);
      }
      */

    if (PausedReason.breakpoint == reason && !isStepping && topFrame != null) {
      final XLineBreakpoint<?> breakpoint = myDebugProcess.getDartBreakpointsHandler().getBreakpointForLocation(topFrame.getLocation());

      if (breakpoint != null) {
        if ("false".equals(evaluateExpression(isolate, topFrame, breakpoint.getConditionExpression()))) {
          resume(isolate);
          return;
        }

        final boolean suspend =
          myDebugProcess.getSession().breakpointReached(breakpoint,
                                                        evaluateExpression(isolate, topFrame, breakpoint.getLogExpressionObject()),
                                                        new DartSuspendContext(myDebugProcess, frames, exception));
        if (!suspend) {
          resume(isolate);
        }

        return;
      }
    }

    myDebugProcess.getSession().positionReached(new DartSuspendContext(myDebugProcess, frames, exception));
  }

  private void resume(final VmIsolate isolate) {
    try {
      myDebugProcess.getVmConnection().resume(isolate);
    }
    catch (IOException ioe) {
      LOG.error(ioe);
    }
  }

  @Nullable
  private String evaluateExpression(final @NotNull VmIsolate isolate,
                                    final @NotNull VmCallFrame topFrame,
                                    final @Nullable XExpression xExpression) {
    final String evalText = xExpression == null ? null : xExpression.getExpression();
    if (StringUtil.isEmptyOrSpaces(evalText)) return null;

    final Ref<String> evalResult = new Ref<String>();
    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    try {
      myDebugProcess.getVmConnection().evaluateOnCallFrame(isolate, topFrame, evalText, new VmCallback<VmValue>() {
        public void handleResult(final VmResult<VmValue> result) {
          final VmValue vmValue = result.getResult();
          if (vmValue != null) {
            evalResult.set(vmValue.getText());
          }
          semaphore.up();
        }
      });
    }
    catch (IOException e) {/**/}

    semaphore.waitFor(1000);
    return evalResult.get();
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
