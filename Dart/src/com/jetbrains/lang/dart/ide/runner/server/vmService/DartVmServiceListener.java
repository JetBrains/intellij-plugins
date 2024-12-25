// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.frame.XValue;
import com.jetbrains.lang.dart.ide.runner.DartExceptionBreakpointProperties;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceValue;
import org.dartlang.vm.service.VmServiceListener;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DartVmServiceListener implements VmServiceListener {
  private static final Logger LOG = Logger.getInstance(DartVmServiceListener.class.getName());

  private final @NotNull DartVmServiceDebugProcess myDebugProcess;
  private final @NotNull DartVmServiceBreakpointHandler myBreakpointHandler;
  private @Nullable XSourcePosition myLatestSourcePosition;

  public DartVmServiceListener(final @NotNull DartVmServiceDebugProcess debugProcess,
                               final @NotNull DartVmServiceBreakpointHandler breakpointHandler) {
    myDebugProcess = debugProcess;
    myBreakpointHandler = breakpointHandler;
  }

  @Override
  public void connectionOpened() {

  }

  @Override
  public void received(final @NotNull String streamId, final @NotNull Event event) {
    switch (event.getKind()) {
      case BreakpointAdded -> {
        // TODO Respond to breakpoints added by the observatory.
        // myBreakpointHandler.vmBreakpointAdded(null, event.getIsolate().getId(), event.getBreakpoint());
      }
      case BreakpointRemoved, Unknown, VMUpdate, VMFlagUpdate, ServiceUnregistered, ServiceRegistered, ServiceExtensionAdded, PauseExit, 
        None, Logging, IsolateUpdate, IsolateStart, IsolateRunnable, IsolateReload, Inspect, GC, Extension -> {
      }
      case BreakpointResolved -> myBreakpointHandler.breakpointResolved(Objects.requireNonNull(event.getBreakpoint()));
      case IsolateExit -> myDebugProcess.isolateExit(Objects.requireNonNull(event.getIsolate()));
      case PauseBreakpoint, PauseException, PauseInterrupted -> {
        myDebugProcess.isolateSuspended(Objects.requireNonNull(event.getIsolate()));
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
          final ElementList<Breakpoint> breakpoints = event.getKind() == EventKind.PauseBreakpoint ? event.getPauseBreakpoints() : null;
          final InstanceRef exception = event.getKind() == EventKind.PauseException ? event.getException() : null;
          onIsolatePaused(event.getIsolate(), breakpoints, exception, event.getTopFrame(), event.getAtAsyncSuspension());
        });
      }
      case PausePostRequest ->
        // We get this event after an isolate reload call, when pause after reload has been requested.
        // This adds the "supports.pausePostRequest" capability.
        myDebugProcess.getVmServiceWrapper().restoreBreakpointsForIsolate(Objects.requireNonNull(event.getIsolate()).getId(),
                                                                          () -> myDebugProcess.getVmServiceWrapper()
                                                                            .resumeIsolate(event.getIsolate().getId(), null));
      case PauseStart -> myDebugProcess.getVmServiceWrapper().handleIsolate(Objects.requireNonNull(event.getIsolate()), true);
      case Resume -> myDebugProcess.isolateResumed(Objects.requireNonNull(event.getIsolate()));
      case WriteEvent -> myDebugProcess.handleWriteEvent(event.getBytes());
    }
  }

  @Override
  public void connectionClosed() {
    if (myDebugProcess.isRemoteDebug()) {
      myDebugProcess.getSession().stop();
    }
  }

  void onIsolatePaused(final @NotNull IsolateRef isolateRef,
                       final @Nullable ElementList<Breakpoint> vmBreakpoints,
                       final @Nullable InstanceRef exception,
                       final @Nullable Frame vmTopFrame,
                       boolean atAsyncSuspension) {
    if (vmTopFrame == null) {
      myDebugProcess.getSession().positionReached(new XSuspendContext() {
      });
      return;
    }

    final DartVmServiceSuspendContext suspendContext =
      new DartVmServiceSuspendContext(myDebugProcess, isolateRef, vmTopFrame, exception, atAsyncSuspension);
    final XStackFrame xTopFrame = suspendContext.getActiveExecutionStack().getTopFrame();
    final XSourcePosition sourcePosition = xTopFrame == null ? null : xTopFrame.getSourcePosition();

    if (vmBreakpoints == null || vmBreakpoints.isEmpty()) {
      final StepOption latestStep = myDebugProcess.getVmServiceWrapper().getLatestStep();
      final StepOption nextStep = atAsyncSuspension ? StepOption.OverAsyncSuspension : latestStep;
      if (latestStep == StepOption.Over && equalSourcePositions(myLatestSourcePosition, sourcePosition)) {
        // continue stepping to change current line
        myDebugProcess.getVmServiceWrapper().resumeIsolate(isolateRef.getId(), nextStep);
      }
      else if (exception != null) {
        final XBreakpoint<DartExceptionBreakpointProperties> breakpoint =
          DartExceptionBreakpointHandler.getDefaultExceptionBreakpoint(myDebugProcess.getSession().getProject());
        final boolean suspend = myDebugProcess.getSession().breakpointReached(breakpoint, null, suspendContext);
        if (!suspend) {
          myDebugProcess.getVmServiceWrapper().resumeIsolate(isolateRef.getId(), null);
        }
      }
      else {
        myLatestSourcePosition = sourcePosition;
        myDebugProcess.getSession().positionReached(suspendContext);
      }
    }
    else {
      if (vmBreakpoints.size() > 1) {
        // Shouldn't happen. IDE doesn't allow to set 2 breakpoints on one line.
        LOG.error(vmBreakpoints.size() + " breakpoints hit in one shot.");
      }

      // Remove any temporary (run to cursor) breakpoints.
      myBreakpointHandler.removeTemporaryBreakpoints(isolateRef.getId());

      final XLineBreakpoint<XBreakpointProperties> xBreakpoint = myBreakpointHandler.getXBreakpoint(vmBreakpoints.get(0));

      if (xBreakpoint == null) {
        // breakpoint could be set in the Observatory
        myLatestSourcePosition = sourcePosition;
        myDebugProcess.getSession().positionReached(suspendContext);
        return;
      }

      if ("false".equals(evaluateExpression(isolateRef.getId(), vmTopFrame, xBreakpoint.getConditionExpression()))) {
        myDebugProcess.getVmServiceWrapper().resumeIsolate(isolateRef.getId(), null);
        return;
      }

      myLatestSourcePosition = sourcePosition;

      final String logExpression = evaluateExpression(isolateRef.getId(), vmTopFrame, xBreakpoint.getLogExpressionObject());
      final boolean suspend = myDebugProcess.getSession().breakpointReached(xBreakpoint, logExpression, suspendContext);
      if (!suspend) {
        myDebugProcess.getVmServiceWrapper().resumeIsolate(isolateRef.getId(), null);
      }
    }
  }

  private static boolean equalSourcePositions(final @Nullable XSourcePosition position1, final @Nullable XSourcePosition position2) {
    return position1 != null &&
           position2 != null &&
           position1.getFile().equals(position2.getFile()) &&
           position1.getLine() == position2.getLine();
  }


  private @Nullable String evaluateExpression(final @NotNull String isolateId,
                                              final @Nullable Frame vmTopFrame,
                                              final @Nullable XExpression xExpression) {
    final String evalText = xExpression == null ? null : xExpression.getExpression();
    if (vmTopFrame == null || StringUtil.isEmptyOrSpaces(evalText)) return null;

    final Ref<String> evalResult = new Ref<>();
    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    myDebugProcess.getVmServiceWrapper().evaluateInFrame(isolateId, vmTopFrame, evalText, new XDebuggerEvaluator.XEvaluationCallback() {
      @Override
      public void evaluated(final @NotNull XValue result) {
        if (result instanceof DartVmServiceValue) {
          evalResult.set(getSimpleStringPresentation(((DartVmServiceValue)result).getInstanceRef()));
        }
        semaphore.up();
      }

      @Override
      public void errorOccurred(final @NotNull String errorMessage) {
        evalResult.set("Failed to evaluate log expression [" + evalText + "]: " + errorMessage);
        semaphore.up();
      }
    });

    semaphore.waitFor(1000);
    return evalResult.get();
  }

  private static @NotNull String getSimpleStringPresentation(final @NotNull InstanceRef instanceRef) {
    // getValueAsString() is provided for the instance kinds: Null, Bool, Double, Int, String (value may be truncated), Float32x4, Float64x2, Int32x4, StackTrace
    return switch (instanceRef.getKind()) {
      case Null, Bool, Double, Int, String, Float32x4, Float64x2, Int32x4, StackTrace ->
        Objects.requireNonNull(instanceRef.getValueAsString());
      default -> "Instance of " + instanceRef.getClassRef().getName();
    };
  }
}
