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
  @NotNull private final DartCommandLineDebugProcess myDebugProcess;
  @NotNull private final DartCommandLineBreakpointHandler myBreakpointsHandler;

  public DartVmListener(@NotNull final DartCommandLineDebugProcess debugProcess,
                        @NotNull final DartCommandLineBreakpointHandler breakpointsHandler) {
    myDebugProcess = debugProcess;
    myBreakpointsHandler = breakpointsHandler;
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
    //myDebugProcess.isolateCreated(isolate); // isolateCreated() will be called when isolate is paused for the first time, otherwise breakpoint handler tries to set initial breakpoints in this isolate

    // do not call BreakpointsHandler.handleIsolateCreated() from here, it is called from first isolatePaused()!

    // Actually enableAllStepping() doesn't do anything except calling getLibraries(). Stepping works without this call.
    // VmConnection.enableAllSteppingSync(isolate);

    try {
      // todo add an option for BreakOnExceptionsType (WEB-13268)
      myDebugProcess.getVmConnection().setPauseOnExceptionSync(isolate, VmConnection.BreakOnExceptionsType.unhandled);
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

  public void isolateShutdown(final VmIsolate isolate) {
    LOG.debug("isolate shutdown: " + isolate.getId());
    myDebugProcess.isolateShutdown(isolate);
    myBreakpointsHandler.handleIsolateShutdown(isolate);
  }

  public void breakpointResolved(final VmIsolate isolate, final VmBreakpoint breakpoint) {
    LOG.debug("breakpoint resolved, isolate = " + breakpoint.getIsolate() + ", id = " + breakpoint.getBreakpointId());
    myBreakpointsHandler.breakpointResolved(breakpoint);
  }

  public void debuggerPaused(final PausedReason reason,
                             final VmIsolate isolate,
                             final List<VmCallFrame> frames,
                             final VmValue exception,
                             final boolean isStepping) {
    LOG.debug("debugger paused, reason: " + reason.name());
    final VmCallFrame topFrame = frames.isEmpty() ? null : frames.get(0);
    final XLineBreakpoint<?> breakpoint;

    if (isolate.isFirstBreak()) {
      isolate.setFirstBreak(false);

      myDebugProcess.isolateCreated(isolate);

      final VmLocation vmLocation = topFrame == null ? null : topFrame.getLocation();
      breakpoint = myBreakpointsHandler.handleIsolateCreatedAndReturnBreakpointAtPosition(isolate, vmLocation);

      // If this is our first break, and there is no user breakpoint here, and the stop is on the main() method, then resume.
      if (PausedReason.breakpoint == reason && breakpoint == null) {
        resume(isolate);
        return;
      }
    }
    else if (PausedReason.breakpoint == reason && !isStepping && topFrame != null) {
      breakpoint = myBreakpointsHandler.getBreakpointForLocation(topFrame.getLocation());
    }
    else {
      breakpoint = null;
    }

    if (breakpoint != null) {
      if ("false".equals(evaluateExpression(isolate, topFrame, breakpoint.getConditionExpression()))) {
        resume(isolate);
        return;
      }

      final boolean suspend =
        myDebugProcess.getSession().breakpointReached(breakpoint,
                                                      evaluateExpression(isolate, topFrame, breakpoint.getLogExpressionObject()),
                                                      new DartSuspendContext(myDebugProcess, isolate, frames, exception));
      if (suspend) {
        myDebugProcess.isolateSuspended(isolate);
      }
      else {
        resume(isolate);
      }

      return;
    }

    final DartSuspendContext suspendContext = new DartSuspendContext(myDebugProcess, isolate, frames, exception);
    myDebugProcess.getSession().positionReached(suspendContext);
    myDebugProcess.isolateSuspended(isolate);
    suspendContext.selectUpperNavigatableStackFrame(myDebugProcess.getSession());
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
                                    final @Nullable VmCallFrame topFrame,
                                    final @Nullable XExpression xExpression) {
    final String evalText = xExpression == null ? null : xExpression.getExpression();
    if (topFrame == null || StringUtil.isEmptyOrSpaces(evalText)) return null;

    final Ref<String> evalResult = new Ref<>();
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

  public void debuggerResumed(final VmIsolate isolate) {
    LOG.debug("debugger resumed: " + isolate.getId());
    myDebugProcess.isolateResumed(isolate);
  }
}
