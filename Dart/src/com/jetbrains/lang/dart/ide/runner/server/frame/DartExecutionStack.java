package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartExecutionStack extends XExecutionStack {
  private final @NotNull DartCommandLineDebugProcess myDebugProcess;
  private @Nullable DartStackFrame myTopFrame;
  private final @NotNull List<VmCallFrame> myVmCallFrames;

  public DartExecutionStack(final @NotNull DartCommandLineDebugProcess debugProcess, final @NotNull List<VmCallFrame> vmCallFrames) {
    super("");
    myDebugProcess = debugProcess;
    myTopFrame = vmCallFrames.isEmpty() ? null : new DartStackFrame(myDebugProcess, vmCallFrames.get(0));
    myVmCallFrames = vmCallFrames;
  }

  @Override
  @Nullable
  public XStackFrame getTopFrame() {
    return myTopFrame;
  }

  @Override
  public void computeStackFrames(int firstFrameIndex, final XStackFrameContainer container) {
    List<DartStackFrame> res = new ArrayList<DartStackFrame>();
    // add top frame if needed
    if (firstFrameIndex == 0 && !myVmCallFrames.isEmpty()) {
      res.add(myTopFrame);
      firstFrameIndex = 1;
    }

    List<VmCallFrame> toAdd = firstFrameIndex >= myVmCallFrames.size() ?
                              Collections.<VmCallFrame>emptyList()
                              : myVmCallFrames.subList(firstFrameIndex, myVmCallFrames.size());

    for (VmCallFrame frame : toAdd) {
      res.add(new DartStackFrame(myDebugProcess, frame));
    }

    container.addStackFrames(res, true);
  }
}
