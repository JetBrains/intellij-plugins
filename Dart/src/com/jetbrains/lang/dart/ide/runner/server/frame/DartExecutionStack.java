package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallFrame;
import com.jetbrains.lang.dart.ide.runner.server.google.VmValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartExecutionStack extends XExecutionStack {
  @NotNull private final List<DartStackFrame> myStackFrames;

  public DartExecutionStack(@NotNull final DartCommandLineDebugProcess debugProcess,
                            @NotNull final List<VmCallFrame> vmCallFrames,
                            @Nullable VmValue exception) {
    super("");
    myStackFrames = new ArrayList<DartStackFrame>(vmCallFrames.size());
    for (VmCallFrame vmCallFrame : vmCallFrames) {
      myStackFrames.add(new DartStackFrame(debugProcess, vmCallFrame, exception));
      exception = null; // add exception to the top frame only
    }
  }

  @Override
  @Nullable
  public XStackFrame getTopFrame() {
    return myStackFrames.isEmpty() ? null : myStackFrames.get(0);
  }

  @Override
  public void computeStackFrames(int firstFrameIndex, @NotNull final XStackFrameContainer container) {
    final List<DartStackFrame> result = firstFrameIndex == 0 ? myStackFrames : myStackFrames.subList(firstFrameIndex, myStackFrames.size());
    container.addStackFrames(result, true);
  }
}
