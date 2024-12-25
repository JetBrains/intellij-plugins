// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.vmService.IsolatesInfo;
import org.dartlang.vm.service.element.Frame;
import org.dartlang.vm.service.element.InstanceRef;
import org.dartlang.vm.service.element.IsolateRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DartVmServiceSuspendContext extends XSuspendContext {
  private final @NotNull DartVmServiceDebugProcess myDebugProcess;
  private final @NotNull DartVmServiceExecutionStack myActiveExecutionStack;

  private List<XExecutionStack> myExecutionStacks;
  private final boolean myAtAsyncSuspension;

  public DartVmServiceSuspendContext(final @NotNull DartVmServiceDebugProcess debugProcess,
                                     final @NotNull IsolateRef isolateRef,
                                     final @NotNull Frame topFrame,
                                     final @Nullable InstanceRef exception,
                                     boolean atAsyncSuspension) {
    myDebugProcess = debugProcess;
    myActiveExecutionStack = new DartVmServiceExecutionStack(debugProcess, isolateRef.getId(), isolateRef.getName(), topFrame, exception);
    myAtAsyncSuspension = atAsyncSuspension;
  }

  @Override
  public @NotNull XExecutionStack getActiveExecutionStack() {
    return myActiveExecutionStack;
  }

  public boolean getAtAsyncSuspension() {
    return myAtAsyncSuspension;
  }

  @Override
  public void computeExecutionStacks(final @NotNull XExecutionStackContainer container) {
    if (myExecutionStacks == null) {
      final Collection<IsolatesInfo.IsolateInfo> isolateInfos = myDebugProcess.getIsolateInfos();
      myExecutionStacks = new ArrayList<>(isolateInfos.size());
      for (IsolatesInfo.IsolateInfo isolateInfo : isolateInfos) {
        if (isolateInfo.getIsolateId().equals(myActiveExecutionStack.getIsolateId())) {
          myExecutionStacks.add(myActiveExecutionStack);
        }
        else {
          myExecutionStacks
            .add(new DartVmServiceExecutionStack(myDebugProcess, isolateInfo.getIsolateId(), isolateInfo.getIsolateName(), null, null));
        }
      }
    }

    container.addExecutionStack(myExecutionStacks, true);
  }
}
