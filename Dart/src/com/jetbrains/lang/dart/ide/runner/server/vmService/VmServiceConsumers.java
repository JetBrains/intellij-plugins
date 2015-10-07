package com.jetbrains.lang.dart.ide.runner.server.vmService;

import org.dartlang.vm.service.consumer.*;
import org.dartlang.vm.service.element.RPCError;
import org.dartlang.vm.service.element.Sentinel;
import org.dartlang.vm.service.element.Success;

public class VmServiceConsumers {

  public static final SuccessConsumer EMPTY_SUCCESS_CONSUMER = new SuccessConsumer() {
    @Override
    public void received(Success response) {
    }

    @Override
    public void onError(RPCError error) {
    }
  };

  private static abstract class ConsumerWrapper implements Consumer {
    @Override
    public void onError(RPCError error) {
    }
  }

  public static abstract class VmConsumerWrapper extends ConsumerWrapper implements VMConsumer {
  }

  public static abstract class GetIsolateConsumerWrapper extends ConsumerWrapper implements GetIsolateConsumer {
    @Override
    public void received(Sentinel response) {
    }
  }

  public static abstract class BreakpointConsumerWrapper implements BreakpointConsumer {
    abstract void sourcePositionNotApplicable();
  }
}
