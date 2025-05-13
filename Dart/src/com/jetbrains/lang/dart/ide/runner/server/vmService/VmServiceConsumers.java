// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.vmService;

import org.dartlang.vm.service.consumer.*;
import org.dartlang.vm.service.element.*;

import java.util.List;

public final class VmServiceConsumers {

  public static final SuccessConsumer EMPTY_SUCCESS_CONSUMER = new SuccessConsumer() {
    @Override
    public void received(Success response) {
    }

    @Override
    public void onError(RPCError error) {
    }
  };

  private abstract static class ConsumerWrapper implements Consumer {
    @Override
    public void onError(RPCError error) {
    }
  }

  abstract static class SuccessConsumerWrapper extends ConsumerWrapper implements SuccessConsumer {
  }

  abstract static class VmConsumerWrapper extends ConsumerWrapper implements VMConsumer {
  }

  abstract static class GetIsolateConsumerWrapper extends ConsumerWrapper implements GetIsolateConsumer {
    @Override
    public void received(Sentinel response) {
    }
  }

  public abstract static class BreakpointsConsumer {
    abstract void received(List<Breakpoint> breakpointResponses, List<RPCError> errorResponses);

    abstract void sourcePositionNotApplicable();
  }

  public abstract static class InvokeConsumerWrapper implements InvokeConsumer {
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

    public abstract void noGoodResult();
  }

  abstract static class EmptyResumeConsumer extends ConsumerWrapper implements ResumeConsumer {
    @Override
    public void received(Sentinel response) {
    }

    @Override
    public void received(Success response) {
    }
  }
}
