// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService;

import org.dartlang.vm.service.consumer.*;
import org.dartlang.vm.service.element.ErrorRef;
import org.dartlang.vm.service.element.RPCError;
import org.dartlang.vm.service.element.Sentinel;
import org.dartlang.vm.service.element.Success;

public final class VmServiceConsumers {

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

  public static abstract class SuccessConsumerWrapper extends ConsumerWrapper implements SuccessConsumer {
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

  public static abstract class EvaluateConsumerWrapper implements EvaluateConsumer {
    @Override
    public final void received(ErrorRef response) {
      noGoodResult();
    }

    @Override
    public final void received(Sentinel response) {
      noGoodResult();
    }

    @Override
    public final void onError(RPCError error) {
      noGoodResult();
    }

    abstract public void noGoodResult();
  }

  public static abstract class InvokeConsumerWrapper implements InvokeConsumer {
    @Override
    public final void received(ErrorRef response) {
      noGoodResult();
    }

    @Override
    public final void received(Sentinel response) {
      noGoodResult();
    }

    @Override
    public final void onError(RPCError error) {
      noGoodResult();
    }

    abstract public void noGoodResult();
  }
}
