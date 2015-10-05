package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.vmService.IsolatesInfo;
import org.dartlang.vm.service.element.Frame;
import org.dartlang.vm.service.element.IsolateRef;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class DartVmServiceSuspendedContext extends XSuspendContext {
  @NotNull private DartVmServiceDebugProcess myDebugProcess;
  @NotNull private final DartVmServiceExecutionStack myActiveExecutionStack;

  private XExecutionStack[] myExecutionStacks;

  public DartVmServiceSuspendedContext(@NotNull final DartVmServiceDebugProcess debugProcess,
                                       @NotNull final IsolateRef isolateRef,
                                       @NotNull final Frame topFrame) {
    myDebugProcess = debugProcess;
    myActiveExecutionStack = new DartVmServiceExecutionStack(debugProcess, isolateRef.getId(), isolateRef.getName(), topFrame);
  }

  @NotNull
  @Override
  public XExecutionStack getActiveExecutionStack() {
    return myActiveExecutionStack;
  }

  @Override
  public XExecutionStack[] getExecutionStacks() {
    if (myExecutionStacks == null) {
      final Collection<IsolatesInfo.IsolateInfo> isolateInfos = myDebugProcess.getIsolateInfos();
      final Collection<XExecutionStack> stacks = new ArrayList<XExecutionStack>(isolateInfos.size());
      for (IsolatesInfo.IsolateInfo isolateInfo : isolateInfos) {
        if (isolateInfo.getIsolateId().equals(myActiveExecutionStack.getIsolateId())) {
          stacks.add(myActiveExecutionStack);
        }
        else {
          stacks.add(new DartVmServiceExecutionStack(myDebugProcess, isolateInfo.getIsolateId(), isolateInfo.getIsolateName(), null));
        }
      }

      myExecutionStacks = stacks.toArray(new XExecutionStack[stacks.size()]);
    }

    return myExecutionStacks;
  }
}
