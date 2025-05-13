// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.google.common.collect.Lists;
import com.google.common.net.PercentEscaper;
import com.google.gson.JsonObject;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.xdebugger.XDebugSession;
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
import org.dartlang.vm.service.element.*;
import org.dartlang.vm.service.element.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class VmServiceWrapper implements Disposable {

  public static final Logger LOG = Logger.getInstance(VmServiceWrapper.class.getName());
  private static final long RESPONSE_WAIT_TIMEOUT = 3000; // millis

  private final DartVmServiceDebugProcess myDebugProcess;
  private final VmService myVmService;
  private final DartVmServiceListener myVmServiceListener;
  private final IsolatesInfo myIsolatesInfo;
  private final DartVmServiceBreakpointHandler myBreakpointHandler;
  private final Alarm myRequestsScheduler;

  private long myVmServiceReceiverThreadId;

  private @Nullable StepOption myLatestStep;

  public VmServiceWrapper(@NotNull DartVmServiceDebugProcess debugProcess,
                          @NotNull VmService vmService,
                          @NotNull DartVmServiceListener vmServiceListener,
                          @NotNull IsolatesInfo isolatesInfo,
                          @NotNull DartVmServiceBreakpointHandler breakpointHandler) {
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

  private void addRequest(@NotNull Runnable runnable) {
    if (!myRequestsScheduler.isDisposed()) {
      myRequestsScheduler.addRequest(runnable, 0);
    }
  }

  public @Nullable StepOption getLatestStep() {
    return myLatestStep;
  }

  private void assertSyncRequestAllowed() {
    ApplicationManager.getApplication().assertIsNonDispatchThread();
    ApplicationManager.getApplication().assertReadAccessNotAllowed();
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

  private void streamListen(@NotNull String streamId, @NotNull SuccessConsumer consumer) {
    addRequest(() -> myVmService.streamListen(streamId, consumer));
  }

  private void getVm(@NotNull VMConsumer consumer) {
    addRequest(() -> myVmService.getVM(consumer));
  }

  public @NotNull CompletableFuture<Isolate> getCachedIsolate(@NotNull String isolateId) {
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

  private void getIsolate(@NotNull String isolateId, @NotNull GetIsolateConsumer consumer) {
    addRequest(() -> myVmService.getIsolate(isolateId, consumer));
  }

  public void handleIsolate(@NotNull IsolateRef isolateRef, boolean isolatePausedStart) {
    // We should auto-resume on a StartPaused event, if we're not remote debugging, and after breakpoints have been set.

    final boolean newIsolate = myIsolatesInfo.addIsolate(isolateRef);

    if (isolatePausedStart) {
      myIsolatesInfo.setShouldInitialResume(isolateRef);
    }

    // Just to make sure that the main isolate is not handled twice, both from handleDebuggerConnected() and DartVmServiceListener.received(PauseStart)
    if (newIsolate) {
      setIsolatePauseMode(isolateRef.getId(), myDebugProcess.getBreakOnExceptionMode(), isolateRef);
    }
    else {
      checkInitialResume(isolateRef);
    }
  }

  private void setIsolatePauseMode(@NotNull String isolateId, @NotNull ExceptionPauseMode mode, @NotNull IsolateRef isolateRef) {
    if (supportsSetIsolatePauseMode()) {
      SetIsolatePauseModeConsumer sipmc = new SetIsolatePauseModeConsumer() {
        @Override
        public void onError(RPCError error) {
        }

        @Override
        public void received(Sentinel response) {
        }

        @Override
        public void received(Success response) {
          setInitialBreakpointsAndResume(isolateRef);
        }
      };
      addRequest(() -> myVmService.setIsolatePauseMode(isolateId, mode, false, sipmc));
    }
    else {
      SetExceptionPauseModeConsumer wrapper = new SetExceptionPauseModeConsumer() {
        @Override
        public void onError(RPCError error) {
        }

        @Override
        public void received(Sentinel response) {
        }

        @Override
        public void received(Success response) {
          setInitialBreakpointsAndResume(isolateRef);
        }
      };
      //noinspection deprecation
      addRequest(() -> myVmService.setExceptionPauseMode(isolateId, mode, wrapper));
    }
  }

  private void checkInitialResume(IsolateRef isolateRef) {
    if (myIsolatesInfo.getShouldInitialResume(isolateRef)) {
      resumeIsolate(isolateRef.getId(), null);
    }
  }

  private void setInitialBreakpointsAndResume(@NotNull IsolateRef isolateRef) {
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

  private void doSetInitialBreakpointsAndResume(@NotNull IsolateRef isolateRef) {
    doSetBreakpointsForIsolate(myBreakpointHandler.getXBreakpoints(), isolateRef.getId(), () -> {
      myIsolatesInfo.setBreakpointsSet(isolateRef);
      checkInitialResume(isolateRef);
    });
  }

  private void doSetBreakpointsForIsolate(@NotNull Set<XLineBreakpoint<XBreakpointProperties>> xBreakpoints,
                                          @NotNull String isolateId,
                                          @Nullable Runnable onFinished) {
    if (xBreakpoints.isEmpty()) {
      if (onFinished != null) {
        onFinished.run();
      }
      return;
    }

    final AtomicInteger counter = new AtomicInteger(xBreakpoints.size());

    for (final XLineBreakpoint<XBreakpointProperties> xBreakpoint : xBreakpoints) {
      addBreakpoint(isolateId, xBreakpoint.getSourcePosition(), new VmServiceConsumers.BreakpointsConsumer() {
        @Override
        void sourcePositionNotApplicable() {
          myBreakpointHandler.breakpointFailed(xBreakpoint);
          checkDone();
        }

        @Override
        public void received(List<Breakpoint> breakpointResponses, List<RPCError> errorResponses) {
          if (!breakpointResponses.isEmpty()) {
            for (Breakpoint breakpoint : breakpointResponses) {
              myBreakpointHandler.vmBreakpointAdded(xBreakpoint, isolateId, breakpoint);
            }
          }
          else if (!errorResponses.isEmpty()) {
            myBreakpointHandler.breakpointFailed(xBreakpoint);
          }
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

  public void addBreakpoint(@NotNull String isolateId,
                            @Nullable XSourcePosition position,
                            @NotNull VmServiceConsumers.BreakpointsConsumer consumer) {
    if (position == null || !FileTypeRegistry.getInstance().isFileOfType(position.getFile(), DartFileType.INSTANCE)) {
      consumer.sourcePositionNotApplicable();
      return;
    }
    if (supportsLookupPackageUris()) {
      addBreakpointWithVmService(isolateId, position, consumer);
    }
    else {
      addBreakpointWithMapper(isolateId, position, consumer);
    }
  }

  // This is the old way of mapping breakpoints, which uses analyzer.
  public void addBreakpointWithMapper(@NotNull String isolateId,
                                      @NotNull XSourcePosition position,
                                      @NotNull VmServiceConsumers.BreakpointsConsumer consumer) {
    addRequest(() -> {
      int line = position.getLine() + 1;

      Collection<String> scriptUris = myDebugProcess.getUrisForFile(position.getFile());
      List<Breakpoint> breakpointResponses = new ArrayList<>();
      List<RPCError> errorResponses = new ArrayList<>();

      for (String uri : scriptUris) {
        myVmService.addBreakpointWithScriptUri(isolateId, uri, line, new AddBreakpointWithScriptUriConsumer() {
          @Override
          public void received(Breakpoint response) {
            breakpointResponses.add(response);
            checkDone();
          }

          @Override
          public void received(Sentinel response) {
            checkDone();
          }

          @Override
          public void onError(RPCError error) {
            errorResponses.add(error);

            checkDone();
          }

          private void checkDone() {
            if (scriptUris.size() == breakpointResponses.size() + errorResponses.size()) {
              consumer.received(breakpointResponses, errorResponses);
            }
          }
        });
      }
    });
  }

  public void addBreakpointWithVmService(@NotNull String isolateId,
                                         @NotNull XSourcePosition position,
                                         @NotNull VmServiceConsumers.BreakpointsConsumer consumer) {
    addRequest(() -> {
      int line = position.getLine() + 1;

      String resolvedUri = getResolvedUri(position);
      LOG.info("Computed resolvedUri: " + resolvedUri);
      List<String> resolvedUriList = List.of(percentEscapeUri(resolvedUri));

      List<Breakpoint> breakpointResponses = new ArrayList<>();
      List<RPCError> errorResponses = new ArrayList<>();

      myVmService.lookupPackageUris(isolateId, resolvedUriList, new UriListConsumer() {
        @Override
        public void received(UriList response) {
          LOG.info("in received of lookupPackageUris");
          if (myDebugProcess.getSession().getProject().isDisposed()) {
            return;
          }

          List<String> uris = response.getUris();

          if (uris == null || uris.get(0) == null) {
            LOG.info("Uri was not found");
            JsonObject error = new JsonObject();
            error.addProperty("error", "Breakpoint could not be mapped to package URI");
            errorResponses.add(new RPCError(error));

            consumer.received(breakpointResponses, errorResponses);
            return;
          }

          String scriptUri = uris.get(0);
          LOG.info("in received of lookupPackageUris. scriptUri: " + scriptUri);
          myVmService.addBreakpointWithScriptUri(isolateId, scriptUri, line, new AddBreakpointWithScriptUriConsumer() {
            @Override
            public void received(Breakpoint response) {
              breakpointResponses.add(response);
              checkDone();
            }

            @Override
            public void received(Sentinel response) {
              checkDone();
            }

            @Override
            public void onError(RPCError error) {
              errorResponses.add(error);

              checkDone();
            }

            private void checkDone() {
              consumer.received(breakpointResponses, errorResponses);
            }
          });
        }

        @Override
        public void onError(RPCError error) {
          LOG.warn("VmService.lookupPackageUris() failed with error code " + error.getCode() +
                   "\n  message: " + error.getMessage() +
                   "\n  details: " + error.getDetails() +
                   "\n  request: " + error.getRequest());
          errorResponses.add(error);
          consumer.received(breakpointResponses, errorResponses);
        }
      });
    });
  }

  private String getResolvedUri(@NotNull XSourcePosition position) {
    XDebugSession session = myDebugProcess.getSession();
    assert session != null;
    VirtualFile file = position.getFile().getCanonicalFile();
    assert file != null;
    String url = file.getUrl();
    LOG.info("in getResolvedUri. url: " + url);

    if (SystemInfo.isWindows) {
      // Dart and the VM service use three /'s in file URIs: https://api.dart.dev/stable/2.16.1/dart-core/Uri-class.html.
      return url.replace("file://", "file:///");
    }

    return url;
  }

  private static String percentEscapeUri(String uri) {
    PercentEscaper escaper = new PercentEscaper("!#$&'()*+,-./:;=?@_~", false);
    return escaper.escape(uri);
  }

  public void addBreakpointForIsolates(@NotNull XLineBreakpoint<XBreakpointProperties> xBreakpoint,
                                       @NotNull Collection<IsolatesInfo.IsolateInfo> isolateInfos) {
    for (final IsolatesInfo.IsolateInfo isolateInfo : isolateInfos) {
      addBreakpoint(isolateInfo.getIsolateId(), xBreakpoint.getSourcePosition(), new VmServiceConsumers.BreakpointsConsumer() {
        @Override
        void sourcePositionNotApplicable() {
        }

        @Override
        public void received(List<Breakpoint> breakpointResponses, List<RPCError> errorResponses) {
          for (Breakpoint breakpoint : breakpointResponses) {
            myBreakpointHandler.vmBreakpointAdded(xBreakpoint, isolateInfo.getIsolateId(), breakpoint);
          }
        }
      });
    }
  }

  /**
   * Reloaded scripts need to have their breakpoints re-applied; re-set all existing breakpoints.
   */
  public void restoreBreakpointsForIsolate(@NotNull String isolateId, @Nullable Runnable onFinished) {
    // Cached information about the isolate may now be stale.
    myIsolatesInfo.invalidateCache(isolateId);

    // Remove all existing VM breakpoints for this isolate.
    myBreakpointHandler.removeAllVmBreakpoints(isolateId);
    // Re-set existing breakpoints.
    doSetBreakpointsForIsolate(myBreakpointHandler.getXBreakpoints(), isolateId, onFinished);
  }

  public void addTemporaryBreakpoint(@NotNull XSourcePosition position, @NotNull String isolateId) {
    addBreakpoint(isolateId, position, new VmServiceConsumers.BreakpointsConsumer() {
      @Override
      void sourcePositionNotApplicable() {
      }

      @Override
      public void received(List<Breakpoint> breakpointResponses, List<RPCError> errorResponses) {
        for (Breakpoint breakpoint : breakpointResponses) {
          myBreakpointHandler.temporaryBreakpointAdded(isolateId, breakpoint);
        }
      }
    });
  }

  public void removeBreakpoint(@NotNull String isolateId, @NotNull String vmBreakpointId) {
    addRequest(() -> myVmService.removeBreakpoint(isolateId, vmBreakpointId, new RemoveBreakpointConsumer() {
      @Override
      public void onError(RPCError error) {
      }

      @Override
      public void received(Sentinel response) {
      }

      @Override
      public void received(Success response) {
      }
    }));
  }

  public void resumeIsolate(@NotNull String isolateId, @Nullable StepOption stepOption) {
    addRequest(() -> {
      myLatestStep = stepOption;
      myVmService.resume(isolateId, stepOption, null, new VmServiceConsumers.EmptyResumeConsumer() {
      });
    });
  }

  public void setExceptionPauseMode(@NotNull ExceptionPauseMode mode) {
    for (final IsolatesInfo.IsolateInfo isolateInfo : myIsolatesInfo.getIsolateInfos()) {
      if (supportsSetIsolatePauseMode()) {
        addRequest(() -> myVmService.setIsolatePauseMode(isolateInfo.getIsolateId(), mode, false, new SetIsolatePauseModeConsumer() {
          @Override
          public void onError(RPCError error) { }

          @Override
          public void received(Sentinel response) { }

          @Override
          public void received(Success response) { }
        }));
      }
      else {
        //noinspection deprecation
        addRequest(() -> myVmService.setExceptionPauseMode(isolateInfo.getIsolateId(), mode, new SetExceptionPauseModeConsumer() {
          @Override
          public void onError(RPCError error) { }

          @Override
          public void received(Sentinel response) { }

          @Override
          public void received(Success response) { }
        }));
      }
    }
  }

  /**
   * Drop to the indicated frame.
   * <p>
   * frameIndex specifies the stack frame to rewind to. Stack frame 0 is the currently executing
   * function, so frameIndex must be at least 1.
   */
  public void dropFrame(@NotNull String isolateId, int frameIndex) {
    addRequest(() -> {
      myLatestStep = StepOption.Rewind;
      myVmService.resume(isolateId, StepOption.Rewind, frameIndex, new VmServiceConsumers.EmptyResumeConsumer() {
        @Override
        public void onError(RPCError error) {
          myDebugProcess.getSession().getConsoleView()
            .print("Error from drop frame: " + error.getMessage() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        }
      });
    });
  }

  public void pauseIsolate(@NotNull String isolateId) {
    addRequest(() -> myVmService.pause(isolateId, new PauseConsumer() {
      @Override
      public void onError(RPCError error) {
      }

      @Override
      public void received(Sentinel response) {
      }

      @Override
      public void received(Success response) {
      }
    }));
  }

  public void computeStackFrames(@NotNull String isolateId,
                                 int firstFrameIndex,
                                 @NotNull XExecutionStack.XStackFrameContainer container,
                                 @Nullable InstanceRef exception) {
    addRequest(() -> myVmService.getStack(isolateId, new GetStackConsumer() {
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
        @NlsSafe String message = error.getMessage();
        container.errorOccurred(message);
      }

      @Override
      public void received(Sentinel response) {
        @NlsSafe String message = response.getValueAsString();
        container.errorOccurred(message);
      }
    }));
  }

  public @Nullable Script getScriptSync(@NotNull String isolateId, @NotNull String scriptId) {
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

  public void getObject(@NotNull String isolateId, @NotNull String objectId, @NotNull GetObjectConsumer consumer) {
    addRequest(() -> myVmService.getObject(isolateId, objectId, consumer));
  }

  public void getCollectionObject(@NotNull String isolateId,
                                  @NotNull String objectId,
                                  int offset,
                                  int count,
                                  @NotNull GetObjectConsumer consumer) {
    addRequest(() -> myVmService.getObject(isolateId, objectId, offset, count, consumer));
  }

  public void evaluateInFrame(@NotNull String isolateId,
                              @NotNull Frame vmFrame,
                              @NotNull String expression,
                              @NotNull XDebuggerEvaluator.XEvaluationCallback callback) {
    addRequest(() -> myVmService.evaluateInFrame(isolateId, vmFrame.getIndex(), expression, new EvaluateInFrameConsumer() {
      @Override
      public void received(InstanceRef instanceRef) {
        callback.evaluated(new DartVmServiceValue(myDebugProcess, isolateId, "result", instanceRef, null, null, false));
      }

      @Override
      public void received(Sentinel sentinel) {
        @NlsSafe String message = sentinel.getValueAsString();
        callback.errorOccurred(message);
      }

      @Override
      public void received(ErrorRef errorRef) {
        callback.errorOccurred(DartVmServiceEvaluator.getPresentableError(errorRef.getMessage()));
      }

      @Override
      public void onError(RPCError error) {
        @NlsSafe String message = error.getMessage();
        callback.errorOccurred(message);
      }
    }));
  }

  @SuppressWarnings("SameParameterValue")
  public void evaluateInTargetContext(@NotNull String isolateId,
                                      @NotNull String targetId,
                                      @NotNull String expression,
                                      @NotNull EvaluateConsumer consumer) {
    addRequest(() -> myVmService.evaluate(isolateId, targetId, expression, consumer));
  }

  public void evaluateInTargetContext(@NotNull String isolateId,
                                      @NotNull String targetId,
                                      @NotNull String expression,
                                      @NotNull XDebuggerEvaluator.XEvaluationCallback callback) {
    evaluateInTargetContext(isolateId, targetId, expression, new EvaluateConsumer() {
      @Override
      public void received(InstanceRef instanceRef) {
        callback.evaluated(new DartVmServiceValue(myDebugProcess, isolateId, "result", instanceRef, null, null, false));
      }

      @Override
      public void received(Sentinel sentinel) {
        @NlsSafe String message = sentinel.getValueAsString();
        callback.errorOccurred(message);
      }

      @Override
      public void received(ErrorRef errorRef) {
        callback.errorOccurred(DartVmServiceEvaluator.getPresentableError(errorRef.getMessage()));
      }

      @Override
      public void onError(RPCError error) {
        @NlsSafe String message = error.getMessage();
        callback.errorOccurred(message);
      }
    });
  }

  public void callToString(@NotNull String isolateId, @NotNull String targetId, @NotNull InvokeConsumer callback) {
    callMethodOnTarget(isolateId, targetId, "toString", callback);
  }

  public void callToList(@NotNull String isolateId, @NotNull String targetId, @NotNull InvokeConsumer callback) {
    callMethodOnTarget(isolateId, targetId, "toList", callback);
  }

  private void callMethodOnTarget(@NotNull String isolateId,
                                  @NotNull String targetId,
                                  @NotNull String methodName,
                                  @NotNull InvokeConsumer callback) {
    // For 3.11 and after we use "invoke"; before that, we use "eval";
    if (supportsInvoke()) {
      addRequest(() -> myVmService.invoke(isolateId, targetId, methodName, Collections.emptyList(), true, callback));
    }
    else {
      myDebugProcess.getVmServiceWrapper()
        .evaluateInTargetContext(isolateId, targetId, methodName + "()", new EvaluateConsumer() {
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
    Version version = myVmService.getRuntimeVersion();
    return version.getMajor() > 3 ||
           version.getMajor() == 3 && version.getMinor() >= 11;
  }

  private boolean supportsSetIsolatePauseMode() {
    Version version = myVmService.getRuntimeVersion();
    return version.getMajor() > 3 ||
           version.getMajor() == 3 && version.getMinor() >= 53;
  }

  private boolean supportsLookupPackageUris() {
    Version version = myVmService.getRuntimeVersion();
    return version.getMajor() > 3 ||
           version.getMajor() == 3 && version.getMinor() >= 52;
  }
}
