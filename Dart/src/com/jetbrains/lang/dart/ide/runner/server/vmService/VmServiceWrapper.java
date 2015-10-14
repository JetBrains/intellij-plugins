package com.jetbrains.lang.dart.ide.runner.server.vmService;

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
import com.jetbrains.lang.dart.ide.runner.server.frame.DartDebuggerEvaluator;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceValue;
import org.dartlang.vm.service.VmService;
import org.dartlang.vm.service.consumer.*;
import org.dartlang.vm.service.element.*;
import org.dartlang.vm.service.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class VmServiceWrapper implements Disposable {

  public static final Logger LOG = Logger.getInstance(VmServiceWrapper.class.getName());
  private static final long RESPONSE_WAIT_TIMEOUT = 3000; // millis

  private final DartVmServiceDebugProcess myDebugProcess;
  private final VmService myVmService;
  private final IsolatesInfo myIsolatesInfo;
  private final DartVmServiceBreakpointHandler myBreakpointHandler;
  private final Alarm myRequestsScheduler;

  private long myVmServiceReceiverThreadId;

  @Nullable private StepOption myLatestStep;

  public VmServiceWrapper(@NotNull final DartVmServiceDebugProcess debugProcess,
                          @NotNull final VmService vmService,
                          @NotNull final IsolatesInfo isolatesInfo,
                          @NotNull final DartVmServiceBreakpointHandler breakpointHandler) {
    myDebugProcess = debugProcess;
    myBreakpointHandler = breakpointHandler;
    myVmService = vmService;
    myIsolatesInfo = isolatesInfo;
    myRequestsScheduler = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
  }

  @Override
  public void dispose() {
  }

  private void addRequest(@NotNull final Runnable runnable) {
    myRequestsScheduler.addRequest(runnable, 0);
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
                if (vm.getIsolates().size() != 1) {
                  Logging.getLogger().logError("Unexpected number of isolates after VM start: " + vm.getIsolates().size());
                }

                final IsolateRef isolateRef = vm.getIsolates().get(0);
                getIsolate(isolateRef.getId(), new VmServiceConsumers.GetIsolateConsumerWrapper() {
                  @Override
                  public void received(final Isolate isolate) {
                    // if event is not PauseStart it means that PauseStart event will follow later and will be handled by listener
                    if (isolate.getPauseEvent().getKind() == EventKind.PauseStart) {
                      handleIsolatePausedOnStart(isolateRef);
                    }
                  }
                });
              }
            });
          }
        });
      }
    });
  }

  private void streamListen(@NotNull final String streamId, @NotNull final SuccessConsumer consumer) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.streamListen(streamId, consumer);
      }
    });
  }

  private void getVm(@NotNull final VMConsumer consumer) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.getVM(consumer);
      }
    });
  }

  private void getIsolate(@NotNull final String isolateId, @NotNull final GetIsolateConsumer consumer) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.getIsolate(isolateId, consumer);
      }
    });
  }

  public void handleIsolatePausedOnStart(@NotNull final IsolateRef isolateRef) {
    // Just to make sure that the main isolate is not handled twice, both from handleDebuggerConnected() and DartVmServiceListener.received(PauseStart)
    if (myIsolatesInfo.addIsolate(isolateRef)) {
      setInitialBreakpointsAndResume(isolateRef.getId());
    }
  }

  private void setInitialBreakpointsAndResume(@NotNull final String isolateId) {
    final Set<XLineBreakpoint<XBreakpointProperties>> xBreakpoints = myBreakpointHandler.getXBreakpoints();
    if (xBreakpoints.isEmpty()) {
      resumeIsolate(isolateId, null);
      return;
    }

    final AtomicInteger counter = new AtomicInteger(xBreakpoints.size());

    for (final XLineBreakpoint<XBreakpointProperties> xBreakpoint : xBreakpoints) {
      addBreakpoint(isolateId, xBreakpoint, new VmServiceConsumers.BreakpointConsumerWrapper() {
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
          if (counter.decrementAndGet() == 0) {
            resumeIsolate(isolateId, null);
          }
        }
      });
    }
  }

  public void addBreakpoint(@NotNull final String isolateId,
                            @NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint,
                            @NotNull final VmServiceConsumers.BreakpointConsumerWrapper consumer) {
    final XSourcePosition position = xBreakpoint.getSourcePosition();
    if (position == null || position.getFile().getFileType() != DartFileType.INSTANCE) {
      consumer.sourcePositionNotApplicable();
      return;
    }

    addRequest(new Runnable() {
      @Override
      public void run() {
        final String uri = myDebugProcess.getUriForFile(position.getFile());
        final int line = position.getLine() + 1;
        myVmService.addBreakpointWithScriptUri(isolateId, uri, line, 1, consumer);
      }
    });
  }

  public void addBreakpointForIsolates(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint,
                                       @NotNull final Collection<IsolatesInfo.IsolateInfo> isolateInfos) {
    for (final IsolatesInfo.IsolateInfo isolateInfo : isolateInfos) {
      addBreakpoint(isolateInfo.getIsolateId(), xBreakpoint, new VmServiceConsumers.BreakpointConsumerWrapper() {
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

  public void removeBreakpoint(@NotNull final String isolateId, @NotNull final String vmBreakpointId) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.removeBreakpoint(isolateId, vmBreakpointId, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER);
      }
    });
  }

  public void resumeIsolate(@NotNull final String isolateId, @Nullable final StepOption stepOption) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myLatestStep = stepOption;
        myVmService.resume(isolateId, stepOption, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER);
      }
    });
  }

  public void computeStackFrames(@NotNull final String isolateId,
                                 final int firstFrameIndex,
                                 @NotNull final XExecutionStack.XStackFrameContainer container) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.getStack(isolateId, new StackConsumer() {
          @Override
          public void received(final Stack vmStack) {
            ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
              @Override
              public void run() {
                final List<XStackFrame> result = new ArrayList<XStackFrame>(vmStack.getFrames().size());
                for (Frame frame : vmStack.getFrames()) {
                  result.add(new DartVmServiceStackFrame(myDebugProcess, isolateId, frame));
                }
                container.addStackFrames(firstFrameIndex == 0 ? result : result.subList(firstFrameIndex, result.size()), true);
              }
            });
          }

          @Override
          public void onError(final RPCError error) {
            container.errorOccurred(error.getMessage());
          }
        });
      }
    });
  }

  @Nullable
  public Script getScriptSync(@NotNull final String isolateId, @NotNull final String scriptId) {
    assertSyncRequestAllowed();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    final Ref<Script> resultRef = Ref.create();

    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.getObject(isolateId, scriptId, new GetObjectConsumer() {
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
        });
      }
    });

    semaphore.waitFor(RESPONSE_WAIT_TIMEOUT);
    return resultRef.get();
  }

  public void getObject(@NotNull final String isolateId, @NotNull final String objectId, @NotNull final GetObjectConsumer consumer) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.getObject(isolateId, objectId, consumer);
      }
    });
  }

  public void evaluateInFrame(@NotNull final String isolateId,
                              @NotNull final Frame vmFrame,
                              @NotNull final String expression,
                              @NotNull final XDebuggerEvaluator.XEvaluationCallback callback,
                              final boolean reportIfError) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.evaluateInFrame(isolateId, vmFrame.getIndex(), expression, new EvaluateInFrameConsumer() {
          @Override
          public void received(InstanceRef instanceRef) {
            callback.evaluated(new DartVmServiceValue(myDebugProcess, isolateId, "result", instanceRef));
          }

          @Override
          public void received(ErrorRef errorRef) {
            if (reportIfError) {
              callback.errorOccurred(DartDebuggerEvaluator.getPresentableError(errorRef.getMessage()));
            }
          }

          @Override
          public void onError(RPCError error) {
            if (reportIfError) {
              callback.errorOccurred(error.getMessage());
            }
          }
        });
      }
    });
  }

  public void evaluateInTargetContext(@NotNull final String isolateId,
                                      @NotNull final String targetId,
                                      @NotNull final String expression,
                                      @NotNull final EvaluateConsumer consumer) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.evaluate(isolateId, targetId, expression, consumer);
      }
    });
  }
}
