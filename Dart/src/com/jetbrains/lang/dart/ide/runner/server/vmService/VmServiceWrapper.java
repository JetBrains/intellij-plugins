package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Alarm;
import org.dartlang.vm.service.VmService;
import org.dartlang.vm.service.element.IsolateRef;
import org.dartlang.vm.service.element.VM;
import org.jetbrains.annotations.NotNull;

public class VmServiceWrapper implements Disposable {

  public static final Logger LOG = Logger.getInstance(VmServiceWrapper.class.getName());

  @NotNull private final VmService myVmService;
  @NotNull private final Alarm myRequestsScheduler;

  public VmServiceWrapper(@NotNull final VmService vmService) {
    myVmService = vmService;
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
              handleIsolateCreated(isolateRef);
            }
          }
        });
      }
    });
  }

  public void handleIsolateCreated(@NotNull final IsolateRef isolateRef) {
    resumeIsolate(isolateRef);
  }

  public void resumeIsolate(@NotNull final IsolateRef isolateRef) {
    addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.resume(isolateRef.getId(), null, VmServiceConsumers.EMPTY_SUCCESS_CONSUMER);
      }
    });
  }
}
