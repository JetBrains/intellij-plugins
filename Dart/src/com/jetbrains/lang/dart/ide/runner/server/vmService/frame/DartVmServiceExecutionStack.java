package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.dartlang.vm.service.element.Frame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class DartVmServiceExecutionStack extends XExecutionStack {
  private final DartVmServiceDebugProcess myDebugProcess;
  private final String myIsolateId;
  private XStackFrame myTopFrame;

  public DartVmServiceExecutionStack(@NotNull final DartVmServiceDebugProcess debugProcess,
                                     @NotNull final String isolateId,
                                     @NotNull final String isolateName,
                                     @Nullable final Frame topFrame) {
    // topFrame is not null for (and only for) the active execution stack
    super(debugProcess.isIsolateSuspended(isolateId) ? isolateName
                                                     : isolateName + " (running)",
          topFrame != null ? AllIcons.Debugger.ThreadCurrent
                           : debugProcess.isIsolateSuspended(isolateId) ? AllIcons.Debugger.ThreadAtBreakpoint
                                                                        : AllIcons.Debugger.ThreadRunning);
    myDebugProcess = debugProcess;
    myIsolateId = isolateId;
    myTopFrame = new DartVmServiceStackFrame(debugProcess, isolateId);
  }

  @Nullable
  @Override
  public XStackFrame getTopFrame() {
    // engine calls getTopFrame for active execution stack only
    return myTopFrame;
  }

  @Override
  public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
    container.addStackFrames(Collections.<XStackFrame>emptyList(), true); // todo
  }

  public String getIsolateId() {
    return myIsolateId;
  }
}
