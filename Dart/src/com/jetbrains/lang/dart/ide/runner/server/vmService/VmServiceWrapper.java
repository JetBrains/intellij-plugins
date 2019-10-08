// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.google.common.collect.Lists;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Ref;
import com.intellij.util.Alarm;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartAsyncMarkerFrame;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceEvaluator;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceValue;
import org.dartlang.vm.service.VmService;
import org.dartlang.vm.service.consumer.*;
import org.dartlang.vm.service.element.Stack;
import org.dartlang.vm.service.element.*;
import org.dartlang.vm.service.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class VmServiceWrapper implements Disposable {

  public static final Logger LOG = Logger.getInstance(VmServiceWrapper.class.getName());
  private static final long RESPONSE_WAIT_TIMEOUT = 3000; // millis

  private final DartVmServiceDebugProcess myDebugProcess;
  private final VmService myVmService;
  private final DartVmServiceListener myVmServiceListener;
  private final IsolatesInfo myIsolatesInfo;
  private final DartVmServiceBreakpointHandler myBreakpointHandler;
  private final Alarm myRequestsScheduler;

  private long myVmServiceReceiverThreadId;

  @Nullable private StepOption myLatestStep;

  public VmServiceWrapper(@NotNull final DartVmServiceDebugProcess debugProcess,
                          @NotNull final VmService vmService,
                          @NotNull final DartVmServiceListener vmServiceListener,
                          @NotNull final IsolatesInfo isolatesInfo,
                          @NotNull final DartVmServiceBreakpointHandler breakpointHandler) {
    myDebugProcess = debugProcess;
    myVmService = vmService;
    myVmServiceListener = vmServiceListener;
    myIsolatesInfo = isolatesInfo;
    myBreakpointHandler = breakpointHandler;
    myRequestsScheduler = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
  }

  @Override
  public void dispose() {
  }

  private void addRequest(@NotNull final Runnable runnable) {
    if (!myRequestsScheduler.isDisposed()) {
      myRequestsScheduler.addRequest(runnable, 0);
    }
  }

  @Nullable
  public StepOption getLatestStep() {
    return myLatestStep;
  }

  private void assertSyncRequestAllowed() {
    if (ApplicationManager.getApplication().isDispatchThread()) {
      LOG.error("EDT should not be blocked by waiting for for the answer from the Dart debugger");
    }
    if (ApplicationManager.getApplication().isReadAccessAllowed()) {
      LOG.error("Waiting for for the answer from the Dart debugger under read action may lead to EDT freeze");
    }
    if (myVmServiceReceiverThreadId == Thread.currentThread().getId()) {
      LOG.error("Synchronous requests must not be made in Web Socket listening thread: answer will never be received");
    }
  }

  public void handleDebuggerConnected() {
    streamListen(VmService.DEBUG_STREAM_ID, new VmServiceConsumers.SuccessConsumerWrapper() {
      @Override
      public void received(final Success success) {
        myVmServiceReceiverThreadId = Thread.currentThread().getId();
        streamListen(VmService.ISOLATE_STREAM_ID, new VmServiceConsumers.SuccessConsumerWrapper() {
          @Override
          public void received(final Success success) {
            getVm(new VmServiceConsumers.VmConsumerWrapper() {
              @Override
              public void received(final VM vm) {
                if (vm.getIsolates().size() == 0) {
                  Logging.getLogger().logError("No isolates found after VM start: " + vm.getIsolates().size());
                }

                for (final IsolateRef isolateRef : vm.getIsolates()) {
                  getIsolate(isolateRef.getId(), new VmServiceConsumers.GetIsolateConsumerWrapper() {
                    @Override
                    public void received(final Isolate isolate) {
                      final Event event = isolate.getPauseEvent();
                      final EventKind eventKind = event.getKind();

                      // Ignore isolates that are very early in their lifecycle. You can't set breakpoints on them
                      // yet, and we'll get lifecycle events for them later.
                      if (eventKind == EventKind.None) {
                        return;
                      }

                      // if event is not PauseStart it means that PauseStart event will follow later and will be handled by listener
                      handleIsolate(isolateRef, eventKind == EventKind.PauseStart);

                      // Handle the case of isolates paused when we connect (this can come up in remote debugging).
                      if (eventKind == EventKind.PauseBreakpoint ||
                          eventKind == EventKind.PauseException ||
                          eventKind == EventKind.PauseInterrupted) {
                        myDebugProcess.isolateSuspended(isolateRef);

                        ApplicationManager.getApplication().executeOnPooledThread(() -> {
                          final ElementList<Breakpoint> breakpoints =
                            eventKind == EventKind.PauseBreakpoint ? event.getPauseBreakpoints() : null;
                          final InstanceRef exception = eventKind == EventKind.PauseException ? event.getException() : null;
                          myVmServiceListener
                            .onIsolatePaused(isolateRef, breakpoints, exception, event.getTopFrame(), event.getAtAsyncSuspension());
                        });
                      }
                    }
                  });
                }
              }
            });
          }
        });
      }
    });

    if (myDebugProcess.isRemoteDebug()) {
      streamListen(VmService.STDOUT_STREAM_ID, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER);
      streamListen(VmService.STDERR_STREAM_ID, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER);
    }
  }

  private void streamListen(@NotNull final String streamId, @NotNull final SuccessConsumer consumer) {
    addRequest(() -> myVmService.streamListen(streamId, consumer));
  }

  private void getVm(@NotNull final VMConsumer consumer) {
    addRequest(() -> myVmService.getVM(consumer));
  }

  public CompletableFuture<Isolate> getCachedIsolate(@NotNull final String isolateId) {
    return myIsolatesInfo.getCachedIsolate(isolateId, () -> {
      CompletableFuture<Isolate> isolateFuture = new CompletableFuture<>();
      getIsolate(isolateId, new GetIsolateConsumer() {

        @Override
        public void onError(RPCError error) {
          isolateFuture.completeExceptionally(new RuntimeException(error.getMessage()));
        }

        @Override
        public void received(Isolate response) {
          isolateFuture.complete(response);
        }

        @Override
        public void received(Sentinel response) {
          // Unable to get the isolate.
          isolateFuture.complete(null);
        }
      });
      return isolateFuture;
    });
  }

  private void getIsolate(@NotNull final String isolateId, @NotNull final GetIsolateConsumer consumer) {
    addRequest(() -> myVmService.getIsolate(isolateId, consumer));
  }

  public void handleIsolate(@NotNull final IsolateRef isolateRef, final boolean isolatePausedStart) {
    // We should auto-resume on a StartPaused event, if we're not remote debugging, and after breakpoints have been set.

    final boolean newIsolate = myIsolatesInfo.addIsolate(isolateRef);

    if (isolatePausedStart) {
      myIsolatesInfo.setShouldInitialResume(isolateRef);
    }

    // Just to make sure that the main isolate is not handled twice, both from handleDebuggerConnected() and DartVmServiceListener.received(PauseStart)
    if (newIsolate) {
      addRequest(() -> myVmService.setExceptionPauseMode(isolateRef.getId(),
                                                         myDebugProcess.getBreakOnExceptionMode(),
                                                         new VmServiceConsumers.SuccessConsumerWrapper() {
                                                           @Override
                                                           public void received(Success response) {
                                                             setInitialBreakpointsAndResume(isolateRef);
                                                           }
                                                         }));
    }
    else {
      checkInitialResume(isolateRef);
    }
  }

  private void checkInitialResume(IsolateRef isolateRef) {
    if (myIsolatesInfo.getShouldInitialResume(isolateRef)) {
      resumeIsolate(isolateRef.getId(), null);
    }
  }

  private void setInitialBreakpointsAndResume(@NotNull final IsolateRef isolateRef) {
    if (myDebugProcess.isRemoteDebug()) {
      if (myDebugProcess.myRemoteProjectRootUri == null) {
        // need to detect remote project root path before setting breakpoints
        getIsolate(isolateRef.getId(), new VmServiceConsumers.GetIsolateConsumerWrapper() {
          @Override
          public void received(final Isolate isolate) {
            myDebugProcess.guessRemoteProjectRoot(isolate.getLibraries());
            doSetInitialBreakpointsAndResume(isolateRef);
          }
        });
      }
      else {
        doSetInitialBreakpointsAndResume(isolateRef);
      }
    }
    else {
      doSetInitialBreakpointsAndResume(isolateRef);
    }
  }

  private void doSetInitialBreakpointsAndResume(@NotNull final IsolateRef isolateRef) {
    doSetBreakpointsForIsolate(myBreakpointHandler.getXBreakpoints(), isolateRef.getId(), () -> {
      myIsolatesInfo.setBreakpointsSet(isolateRef);
      checkInitialResume(isolateRef);
    });
  }

  private void doSetBreakpointsForIsolate(@NotNull final Set<XLineBreakpoint<XBreakpointProperties>> xBreakpoints,
                                          @NotNull final String isolateId,
                                          @Nullable final Runnable onFinished) {
    if (xBreakpoints.isEmpty()) {
      if (onFinished != null) {
        onFinished.run();
      }
      return;
    }

    final AtomicInteger counter = new AtomicInteger(xBreakpoints.size());

    for (final XLineBreakpoint<XBreakpointProperties> xBreakpoint : xBreakpoints) {
      addBreakpoint(isolateId, xBreakpoint.getSourcePosition(), new VmServiceConsumers.BreakpointConsumerWrapper() {
        @Override
        void sourcePositionNotApplicable() {
          checkDone();
        }

        @Override
        public void received(Breakpoint vmBreakpoint) {
          myBreakpointHandler.vmBreakpointAdded(xBreakpoint, isolateId, vmBreakpoint);
          checkDone();
        }

        @Override
        public void onError(RPCError error) {
          myBreakpointHandler.breakpointFailed(xBreakpoint);
          checkDone();
        }

        private void checkDone() {
          if (counter.decrementAndGet() == 0 && onFinished != null) {
            onFinished.run();
          }
        }
      });
    }
  }

  public void addBreakpoint(@NotNull final String isolateId,
                            @Nullable final XSourcePosition position,
                            @NotNull final VmServiceConsumers.BreakpointConsumerWrapper consumer) {
    if (position == null || position.getFile().getFileType() != DartFileType.INSTANCE) {
      consumer.sourcePositionNotApplicable();
      return;
    }

    addRequest(() -> {
      final int line = position.getLine() + 1;
      for (String uri : myDebugProcess.getUrisForFile(position.getFile())) {
        myVmService.addBreakpointWithScriptUri(isolateId, uri, line, consumer);
      }
    });
  }

  public void addBreakpointForIsolates(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint,
                                       @NotNull final Collection<IsolatesInfo.IsolateInfo> isolateInfos) {
    for (final IsolatesInfo.IsolateInfo isolateInfo : isolateInfos) {
      addBreakpoint(isolateInfo.getIsolateId(), xBreakpoint.getSourcePosition(), new VmServiceConsumers.BreakpointConsumerWrapper() {
        @Override
        void sourcePositionNotApplicable() {
        }

        @Override
        public void received(Breakpoint vmBreakpoint) {
          myBreakpointHandler.vmBreakpointAdded(xBreakpoint, isolateInfo.getIsolateId(), vmBreakpoint);
        }

        @Override
        public void onError(RPCError error) {
        }
      });
    }
  }

  /**
   * Reloaded scripts need to have their breakpoints re-applied; re-set all existing breakpoints.
   */
  public void restoreBreakpointsForIsolate(@NotNull final String isolateId, @Nullable final Runnable onFinished) {
    // Cached information about the isolate may now be stale.
    myIsolatesInfo.invalidateCache(isolateId);

    // Remove all existing VM breakpoints for this isolate.
    myBreakpointHandler.removeAllVmBreakpoints(isolateId);
    // Re-set existing breakpoints.
    doSetBreakpointsForIsolate(myBreakpointHandler.getXBreakpoints(), isolateId, onFinished);
  }

  public void addTemporaryBreakpoint(@NotNull final XSourcePosition position,
                                     @NotNull final String isolateId) {
    addBreakpoint(isolateId, position, new VmServiceConsumers.BreakpointConsumerWrapper() {
      @Override
      void sourcePositionNotApplicable() {
      }

      @Override
      public void received(Breakpoint vmBreakpoint) {
        myBreakpointHandler.temporaryBreakpointAdded(isolateId, vmBreakpoint);
      }

      @Override
      public void onError(RPCError error) {
      }
    });
  }

  public void removeBreakpoint(@NotNull final String isolateId, @NotNull final String vmBreakpointId) {
    addRequest(() -> myVmService.removeBreakpoint(isolateId, vmBreakpointId, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER));
  }

  public void resumeIsolate(@NotNull final String isolateId, @Nullable final StepOption stepOption) {
    addRequest(() -> {
      myLatestStep = stepOption;
      myVmService.resume(isolateId, stepOption, null, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER);
    });
  }

  public void setExceptionPauseMode(@NotNull final ExceptionPauseMode mode) {
    for (final IsolatesInfo.IsolateInfo isolateInfo : myIsolatesInfo.getIsolateInfos()) {
      addRequest(() -> myVmService.setExceptionPauseMode(isolateInfo.getIsolateId(), mode, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER));
    }
  }

  /**
   * Drop to the indicated frame.
   * <p>
   * frameIndex specifies the stack frame to rewind to. Stack frame 0 is the currently executing
   * function, so frameIndex must be at least 1.
   */
  public void dropFrame(@NotNull final String isolateId, int frameIndex) {
    addRequest(() -> {
      myLatestStep = StepOption.Rewind;
      myVmService.resume(isolateId, StepOption.Rewind, frameIndex, new SuccessConsumer() {
        @Override
        public void onError(RPCError error) {
          myDebugProcess.getSession().getConsoleView()
            .print("Error from drop frame: " + error.getMessage() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        }

        @Override
        public void received(Success response) {
        }
      });
    });
  }

  public void pauseIsolate(@NotNull final String isolateId) {
    addRequest(() -> myVmService.pause(isolateId, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER));
  }

  public void computeStackFrames(@NotNull final String isolateId,
                                 final int firstFrameIndex,
                                 @NotNull final XExecutionStack.XStackFrameContainer container,
                                 @Nullable final InstanceRef exception) {
    addRequest(() -> myVmService.getStack(isolateId, new StackConsumer() {
      @Override
      public void received(final Stack vmStack) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
          InstanceRef exceptionToAddToFrame = exception;

          // Check for async causal frames; fall back to using regular sync frames.
          ElementList<Frame> elementList = vmStack.getAsyncCausalFrames();
          if (elementList == null) {
            elementList = vmStack.getFrames();
          }

          final List<Frame> vmFrames = Lists.newArrayList(elementList);
          final List<XStackFrame> xStackFrames = new ArrayList<>(vmFrames.size());

          for (final Frame vmFrame : vmFrames) {
            if (vmFrame.getKind() == FrameKind.AsyncSuspensionMarker) {
              // Render an asynchronous gap.
              final XStackFrame markerFrame = new DartAsyncMarkerFrame();
              xStackFrames.add(markerFrame);
            }
            else {
              final DartVmServiceStackFrame stackFrame =
                new DartVmServiceStackFrame(myDebugProcess, isolateId, vmFrame, vmFrames, exceptionToAddToFrame);
              stackFrame.setIsDroppableFrame(vmFrame.getKind() == FrameKind.Regular);
              xStackFrames.add(stackFrame);

              if (!stackFrame.isInDartSdkPatchFile()) {
                // The exception (if any) is added to the frame where debugger stops and to the upper frames.
                exceptionToAddToFrame = null;
              }
            }
          }
          container.addStackFrames(firstFrameIndex == 0 ? xStackFrames : xStackFrames.subList(firstFrameIndex, xStackFrames.size()), true);
        });
      }

      @Override
      public void onError(final RPCError error) {
        container.errorOccurred(error.getMessage());
      }
    }));
  }

  @Nullable
  public Script getScriptSync(@NotNull final String isolateId, @NotNull final String scriptId) {
    assertSyncRequestAllowed();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    final Ref<Script> resultRef = Ref.create();

    addRequest(() -> myVmService.getObject(isolateId, scriptId, new GetObjectConsumer() {
      @Override
      public void received(Obj script) {
        resultRef.set((Script)script);
        semaphore.up();
      }

      @Override
      public void received(Sentinel response) {
        semaphore.up();
      }

      @Override
      public void onError(RPCError error) {
        semaphore.up();
      }
    }));

    semaphore.waitFor(RESPONSE_WAIT_TIMEOUT);
    return resultRef.get();
  }

  public void getObject(@NotNull final String isolateId, @NotNull final String objectId, @NotNull final GetObjectConsumer consumer) {
    addRequest(() -> myVmService.getObject(isolateId, objectId, consumer));
  }

  public void getCollectionObject(@NotNull final String isolateId,
                                  @NotNull final String objectId,
                                  final int offset,
                                  final int count,
                                  @NotNull final GetObjectConsumer consumer) {
    addRequest(() -> myVmService.getObject(isolateId, objectId, offset, count, consumer));
  }

  public void evaluateInFrame(@NotNull final String isolateId,
                              @NotNull final Frame vmFrame,
                              @NotNull final String expression,
                              @NotNull final XDebuggerEvaluator.XEvaluationCallback callback) {
    addRequest(() -> myVmService.evaluateInFrame(isolateId, vmFrame.getIndex(), expression, new EvaluateInFrameConsumer() {
      @Override
      public void received(InstanceRef instanceRef) {
        callback.evaluated(new DartVmServiceValue(myDebugProcess, isolateId, "result", instanceRef, null, null, false));
      }

      @Override
      public void received(Sentinel sentinel) {
        callback.errorOccurred(sentinel.getValueAsString());
      }

      @Override
      public void received(ErrorRef errorRef) {
        callback.errorOccurred(DartVmServiceEvaluator.getPresentableError(errorRef.getMessage()));
      }

      @Override
      public void onError(RPCError error) {
        callback.errorOccurred(error.getMessage());
      }
    }));
  }

  @SuppressWarnings("SameParameterValue")
  public void evaluateInTargetContext(@NotNull final String isolateId,
                                      @NotNull final String targetId,
                                      @NotNull final String expression,
                                      @NotNull final EvaluateConsumer consumer) {
    addRequest(() -> myVmService.evaluate(isolateId, targetId, expression, consumer));
  }

  public void evaluateInTargetContext(@NotNull final String isolateId,
                                      @NotNull final String targetId,
                                      @NotNull final String expression,
                                      @NotNull final XDebuggerEvaluator.XEvaluationCallback callback) {
    evaluateInTargetContext(isolateId, targetId, expression, new EvaluateConsumer() {
      @Override
      public void received(InstanceRef instanceRef) {
        callback.evaluated(new DartVmServiceValue(myDebugProcess, isolateId, "result", instanceRef, null, null, false));
      }

      @Override
      public void received(Sentinel sentinel) {
        callback.errorOccurred(sentinel.getValueAsString());
      }

      @Override
      public void received(ErrorRef errorRef) {
        callback.errorOccurred(DartVmServiceEvaluator.getPresentableError(errorRef.getMessage()));
      }

      @Override
      public void onError(RPCError error) {
        callback.errorOccurred(error.getMessage());
      }
    });
  }

  public void callToString(@NotNull final String isolateId,
                           @NotNull final String targetId,
                           @NotNull final InvokeConsumer callback) {
    // For 3.11 and after we use "invoke"; before that, we use "eval";
    if (supportsInvoke()) {
      addRequest(() -> myVmService.invoke(isolateId, targetId, "toString", Collections.emptyList(), true, callback));
    }
    else {
      myDebugProcess.getVmServiceWrapper()
        .evaluateInTargetContext(isolateId, targetId, "toString()", new EvaluateConsumer() {
          @Override
          public void onError(RPCError error) {
            callback.onError(error);
          }

          @Override
          public void received(ErrorRef response) {
            callback.received(response);
          }

          @Override
          public void received(InstanceRef response) {
            callback.received(response);
          }

          @Override
          public void received(Sentinel response) {
            callback.received(response);
          }
        });
    }
  }

  /**
   * Return whether the "invoke" call is supported by this connection.
   */
  private boolean supportsInvoke() {
    final Version version = myVmService.getRuntimeVersion();
    return version.getMajor() >= 3 && version.getMinor() >= 11;
  }
}
