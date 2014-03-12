package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DartExecutionStack extends XExecutionStack {
  private final @NotNull DartCommandLineDebugProcess myDebugProcess;
  private @Nullable DartStackFrame myTopFrame;
  private final @NotNull List<VmCallFrame> myVmCallFrames;

  public DartExecutionStack(final @NotNull DartCommandLineDebugProcess debugProcess, final @NotNull List<VmCallFrame> vmCallFrames) {
    super("Dart VM");
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
  public void computeStackFrames(final int firstFrameIndex, final XStackFrameContainer container) {
    final Iterator<VmCallFrame> iterator = myVmCallFrames.iterator();
    if (iterator.hasNext()) iterator.next(); // skip top frame

    while (iterator.hasNext()) {
      final VmCallFrame frame = iterator.next();
      container.addStackFrames(Collections.singletonList(new DartStackFrame(myDebugProcess, frame)), false);
    }

    container.addStackFrames(Collections.<XStackFrame>emptyList(), true);
  }
}
