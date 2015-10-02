package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Alarm;
import org.dartlang.vm.service.VmService;
import org.dartlang.vm.service.consumer.GetLibraryConsumer;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class VmServiceWrapper implements Disposable {

  public static final Logger LOG = Logger.getInstance(VmServiceWrapper.class.getName());

  private final VmService myVmService;
  private final IsolatesInfo myIsolatesInfo;
  private final Alarm myRequestsScheduler;

  public VmServiceWrapper(@NotNull final VmService vmService, @NotNull final IsolatesInfo isolatesInfo) {
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
              resumeIsolateIfDone();
            }

            @Override
            public void onError(RPCError error) {
              resumeIsolateIfDone();
            }

            private void resumeIsolateIfDone() {
              if (counter.decrementAndGet() == 0) {
                resumeIsolate(isolate.getId());
              }
            }
          });
        }
      });
    }
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
