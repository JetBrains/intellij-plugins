package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Alarm;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.DartFileType;
import org.dartlang.vm.service.VmService;
import org.dartlang.vm.service.consumer.GetLibraryConsumer;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class VmServiceWrapper implements Disposable {

  public static final Logger LOG = Logger.getInstance(VmServiceWrapper.class.getName());

  private final DartVmServiceDebugProcess myDebugProcess;
  private final VmService myVmService;
  private final IsolatesInfo myIsolatesInfo;
  private final DartVmServiceBreakpointHandler myBreakpointHandler;
  private final Alarm myRequestsScheduler;

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

  public void streamListen(@NotNull final String streamId) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.streamListen(streamId, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER);
      }
    });
  }

  public void handleDebuggerConnected() {
    // handle existing isolates (there should be one main isolate that is paused on start) and resume
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.getVM(new VmServiceConsumers.VmConsumerWrapper() {
          @Override
          public void received(final VM vm) {
            for (final IsolateRef isolateRef : vm.getIsolates()) {
              handleIsolatePausedOnStart(isolateRef);
            }
          }
        });
      }
    });
  }

  public void handleIsolatePausedOnStart(@NotNull final IsolateRef isolateRef) {
    if (myIsolatesInfo.isIsolateKnown(isolateRef.getId())) {
      // Something strange happens:
      // in most cases VmServiceListener is not notified with EventKind.PauseStart for the main isolate, but sometimes this happens.
      return;
    }

    myIsolatesInfo.addIsolate(isolateRef);

    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.getIsolate(isolateRef.getId(), new VmServiceConsumers.IsolateConsumerWrapper() {
          @Override
          public void received(final Isolate isolate) {
            handleIsolatePausedOnStart(isolate);
          }
        });
      }
    });
  }

  private void handleIsolatePausedOnStart(@NotNull final Isolate isolate) {
    final AtomicInteger counter = new AtomicInteger(isolate.getLibraries().size());

    for (final LibraryRef libraryRef : isolate.getLibraries()) {
      addRequest(new Runnable() {
        @Override
        public void run() {
          myVmService.getLibrary(isolate.getId(), libraryRef.getId(), new GetLibraryConsumer() {
            @Override
            public void received(final Library library) {
              myIsolatesInfo.addLibrary(isolate, library);
              checkDone();
            }

            @Override
            public void onError(RPCError error) {
              checkDone();
            }

            private void checkDone() {
              if (counter.decrementAndGet() == 0) {
                addInitialBreakpointsAndResume(isolate);
              }
            }
          });
        }
      });
    }
  }

  private void addInitialBreakpointsAndResume(@NotNull final Isolate isolate) {
    final Set<XLineBreakpoint<XBreakpointProperties>> xBreakpoints = myBreakpointHandler.getXBreakpoints();
    if (xBreakpoints.isEmpty()) {
      resumeIsolate(isolate.getId());
      return;
    }

    final AtomicInteger counter = new AtomicInteger(xBreakpoints.size());

    for (final XLineBreakpoint<XBreakpointProperties> xBreakpoint : xBreakpoints) {
      addBreakpoint(isolate.getId(), xBreakpoint, new VmServiceConsumers.BreakpointConsumerWrapper() {
        @Override
        void sourcePositionNotApplicable() {
          checkDone();
        }

        @Override
        public void received(Breakpoint vmBreakpoint) {
          myBreakpointHandler.vmBreakpointAdded(xBreakpoint, isolate.getId(), vmBreakpoint);
          checkDone();
        }

        @Override
        public void onError(RPCError error) {
          myBreakpointHandler.breakpointFailed(xBreakpoint);
          checkDone();
        }

        private void checkDone() {
          if (counter.decrementAndGet() == 0) {
            resumeIsolate(isolate.getId());
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

    final String uri = myDebugProcess.getUriForFile(position.getFile());
    final String scriptId = myIsolatesInfo.getScriptId(isolateId, uri);
    final int line = position.getLine() + 1;

    if (scriptId == null) {
      consumer.sourcePositionNotApplicable();
      return;
    }

    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.addBreakpoint(isolateId, scriptId, line, consumer);
      }
    });
  }

  public void addBreakpointForIsolates(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint,
                                       @NotNull final Collection<String> isolateIds) {
    for (final String isolateId : isolateIds) {
      addBreakpoint(isolateId, xBreakpoint, new VmServiceConsumers.BreakpointConsumerWrapper() {
        @Override
        void sourcePositionNotApplicable() {
        }

        @Override
        public void received(Breakpoint vmBreakpoint) {
          myBreakpointHandler.vmBreakpointAdded(xBreakpoint, isolateId, vmBreakpoint);
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

  public void resumeIsolate(@NotNull final String isolateId) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.resume(isolateId, null, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER);
      }
    });
  }

  public void handleIsolateExit(@NotNull final IsolateRef isolateRef) {
    myIsolatesInfo.deleteIsolate(isolateRef);
  }
}
