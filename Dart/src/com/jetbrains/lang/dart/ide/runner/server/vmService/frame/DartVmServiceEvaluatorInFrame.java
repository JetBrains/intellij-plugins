// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.dartlang.vm.service.element.Frame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartVmServiceEvaluatorInFrame extends DartVmServiceEvaluator {

  private final @NotNull String myIsolateId;
  private final @NotNull Frame myFrame;

  public DartVmServiceEvaluatorInFrame(final @NotNull DartVmServiceDebugProcess debugProcess,
                                       final @NotNull String isolateId,
                                       final @NotNull Frame vmFrame) {
    super(debugProcess);
    myIsolateId = isolateId;
    myFrame = vmFrame;
  }

  @Override
  public void evaluate(final @NotNull String expression,
                       final @NotNull XEvaluationCallback callback,
                       final @Nullable XSourcePosition expressionPosition) {
    myDebugProcess.getVmServiceWrapper().evaluateInFrame(myIsolateId, myFrame, expression, callback);
  }
}
