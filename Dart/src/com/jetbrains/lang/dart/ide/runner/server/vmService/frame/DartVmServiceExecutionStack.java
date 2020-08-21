// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.dartlang.vm.service.element.Frame;
import org.dartlang.vm.service.element.InstanceRef;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class DartVmServiceExecutionStack extends XExecutionStack {
  private final DartVmServiceDebugProcess myDebugProcess;
  private final String myIsolateId;
  private final @Nullable XStackFrame myTopFrame;
  private final @Nullable InstanceRef myException;

  public DartVmServiceExecutionStack(@NotNull DartVmServiceDebugProcess debugProcess,
                                     @NotNull String isolateId,
                                     @NotNull String isolateName,
                                     @Nullable Frame topFrame,
                                     @Nullable InstanceRef exception) {
    // topFrame is not null for (and only for) the active execution stack
    super(debugProcess.isIsolateSuspended(isolateId)
          ? beautify(isolateName)
          : DartBundle.message("debugger.isolate.name.running", beautify(isolateName)),
          topFrame != null ? AllIcons.Debugger.ThreadCurrent
                           : debugProcess.isIsolateSuspended(isolateId) ? AllIcons.Debugger.ThreadAtBreakpoint
                                                                        : AllIcons.Debugger.ThreadRunning);
    myDebugProcess = debugProcess;
    myIsolateId = isolateId;
    myException = exception;
    myTopFrame = topFrame == null ? null : new DartVmServiceStackFrame(debugProcess, isolateId, topFrame, null, exception);
  }

  private static @NotNull @Nls String beautify(@NotNull @NlsSafe String isolateName) {
    // in tests it is "foo_test.dart%22%20as%20test;%0A%0A%20%20%20%20%20%20%20%20void%20main(_,%20SendPort%20message)..."
    final int index = isolateName.indexOf(".dart%22%20as%20test;");
    return index > 0 ? isolateName.substring(0, index + ".dart".length()) : isolateName;
  }

  @Override
  public @Nullable XStackFrame getTopFrame() {
    // engine calls getTopFrame for active execution stack only, for which myTopFrame is calculated in constructor
    return myTopFrame;
  }

  @Override
  public void computeStackFrames(int firstFrameIndex, @NotNull XStackFrameContainer container) {
    if (myDebugProcess.isIsolateSuspended(myIsolateId)) {
      myDebugProcess.getVmServiceWrapper().computeStackFrames(myIsolateId, firstFrameIndex, container, myException);
    }
    else {
      container.addStackFrames(Collections.emptyList(), true);
    }
  }

  public String getIsolateId() {
    return myIsolateId;
  }
}
