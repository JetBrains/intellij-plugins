package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallFrame;
import com.jetbrains.lang.dart.ide.runner.server.google.VmIsolate;
import com.jetbrains.lang.dart.ide.runner.server.google.VmValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartExecutionStack extends XExecutionStack {
  @NotNull private final List<DartStackFrame> myStackFrames;

  public DartExecutionStack(@NotNull final DartCommandLineDebugProcess debugProcess,
                            @NotNull final VmIsolate isolate,
                            @NotNull final List<VmCallFrame> vmCallFrames,
                            @Nullable VmValue exception) {
    super("Isolate id=" + isolate.getId());

    myStackFrames = new ArrayList<DartStackFrame>(vmCallFrames.size());

    for (VmCallFrame vmCallFrame : vmCallFrames) {
      final DartStackFrame frame = new DartStackFrame(debugProcess, vmCallFrame, exception);
      myStackFrames.add(frame);

      if (frame.getSourcePosition() != null) {
        // exception (if any) is added to the frame where debugger stops (the highest frame with not null source position) and to the upper frames
        exception = null;
      }
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

  @Nullable
  public DartStackFrame getFrameToStopAt() {
    return ContainerUtil.find(myStackFrames, new Condition<DartStackFrame>() {
      @Override
      public boolean value(final DartStackFrame frame) {
        return frame.getSourcePosition() != null;
      }
    });
  }
}
