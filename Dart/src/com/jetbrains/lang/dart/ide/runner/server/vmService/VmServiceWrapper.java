package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Ref;
import com.intellij.util.Alarm;
import com.intellij.util.concurrency.Semaphore;
import org.dartlang.vm.service.VmService;
import org.dartlang.vm.service.consumer.SuccessConsumer;
import org.dartlang.vm.service.consumer.VMConsumer;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class VmServiceWrapper implements Disposable {

  public static final Logger LOG = Logger.getInstance(VmServiceWrapper.class.getName());

  private static final long RESPONSE_WAIT_TIMEOUT = 5000; // millis

  private static final SuccessConsumer EMPTY_CONSUMER = new SuccessConsumer() {
    @Override
    public void received(Success response) {
    }

    @Override
    public void onError(RPCError error) {
    }
  };

  @NotNull private final VmService myVmService;
  @NotNull private final Alarm myRequestsScheduler;

  public VmServiceWrapper(@NotNull final VmService vmService) {
    myVmService = vmService;
    myRequestsScheduler = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
  }

  @Override
  public void dispose() {
  }

  private static void assertSyncRequestAllowed() {
    if (ApplicationManager.getApplication().isDispatchThread()) {
      LOG.error("EDT should not be blocked by waiting for for the answer from the Dart debugger");
    }
    if (ApplicationManager.getApplication().isReadAccessAllowed()) {
      LOG.error("Waiting for for the answer from the Dart debugger under read action may lead to EDT freeze");
    }
  }

  public void streamListenSync(@NotNull final String streamId) throws VmServiceException {
    assertSyncRequestAllowed();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    final Ref<RPCError> errorRef = Ref.create();

    myRequestsScheduler.addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.streamListen(streamId, new SuccessConsumer() {
          @Override
          public void received(Success response) {
            semaphore.up();
          }

          @Override
          public void onError(RPCError error) {
            errorRef.set(error);
            semaphore.up();
          }
        });
      }
    }, 0);

    semaphore.waitFor(RESPONSE_WAIT_TIMEOUT);

    if (semaphore.tryUp()) {
      throw new VmServiceException("No response from VmService.streamListen(\"" + streamId + "\") in " + RESPONSE_WAIT_TIMEOUT + "ms.");
    }
    if (!errorRef.isNull()) {
      throw new VmServiceException("VmService.streamListen(\"" + streamId + "\")", errorRef.get());
    }
  }

  @NotNull
  private VM getVMSync() throws VmServiceException {
    assertSyncRequestAllowed();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    final Ref<VM> resultRef = Ref.create();
    final Ref<RPCError> errorRef = Ref.create();

    myRequestsScheduler.addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.getVM(new VMConsumer() {
          @Override
          public void received(VM response) {
            resultRef.set(response);
            semaphore.up();
          }

          @Override
          public void onError(RPCError error) {
            errorRef.set(error);
            semaphore.up();
          }
        });
      }
    }, 0);

    semaphore.waitFor(RESPONSE_WAIT_TIMEOUT);

    if (semaphore.tryUp()) {
      throw new VmServiceException("No response from VmService.getVM() in " + RESPONSE_WAIT_TIMEOUT + "ms.");
    }
    if (!errorRef.isNull()) {
      throw new VmServiceException("VmService.getVM()", errorRef.get());
    }

    return resultRef.get();
  }

  public void resumeAllIsolatesSync() throws VmServiceException {
    assertSyncRequestAllowed();

    final VM vm = getVMSync();
    resumeAllIsolatesSync(vm.getIsolates());
  }

  private void resumeAllIsolatesSync(@NotNull final ElementList<IsolateRef> isolateRefs) throws VmServiceException {
    assertSyncRequestAllowed();

    final CountDownLatch latch = new CountDownLatch(isolateRefs.size());

    final Ref<RPCError> errorRef = Ref.create();

    for (final IsolateRef isolateRef : isolateRefs) {
      myRequestsScheduler.addRequest(new Runnable() {
        @Override
        public void run() {
          myVmService.resume(isolateRef.getId(), null, new SuccessConsumer() {
            @Override
            public void received(Success response) {
              latch.countDown();
            }

            @Override
            public void onError(RPCError error) {
              errorRef.set(error);
              latch.countDown();
            }
          });
        }
      }, 0);
    }

    try {
      latch.await(RESPONSE_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      throw new VmServiceException("Thread interrupted while waiting for VmService.resume(). " + e.getMessage());
    }

    if (latch.getCount() > 0) {
      throw new VmServiceException("No response from VmService.resume() in " + RESPONSE_WAIT_TIMEOUT + "ms.");
    }
    if (!errorRef.isNull()) {
      throw new VmServiceException("VmService.resume()", errorRef.get());
    }
  }

  public void resumeIsolate(@NotNull final IsolateRef isolateRef) {
    myRequestsScheduler.addRequest(new Runnable() {
      @Override
      public void run() {
        myVmService.resume(isolateRef.getId(), null, EMPTY_CONSUMER);
      }
    }, 0);
  }
}
