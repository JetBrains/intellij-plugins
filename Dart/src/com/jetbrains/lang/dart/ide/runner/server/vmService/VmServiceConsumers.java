package com.jetbrains.lang.dart.ide.runner.server.vmService;

import org.dartlang.vm.service.consumer.Consumer;
import org.dartlang.vm.service.consumer.IsolateConsumer;
import org.dartlang.vm.service.consumer.SuccessConsumer;
import org.dartlang.vm.service.consumer.VMConsumer;
import org.dartlang.vm.service.element.RPCError;
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

  public static abstract class IsolateConsumerWrapper extends ConsumerWrapper implements IsolateConsumer {
  }
}
