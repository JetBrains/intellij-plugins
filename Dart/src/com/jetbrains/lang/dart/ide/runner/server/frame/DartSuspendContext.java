package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionAdapter;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallFrame;
import com.jetbrains.lang.dart.ide.runner.server.google.VmIsolate;
import com.jetbrains.lang.dart.ide.runner.server.google.VmValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartSuspendContext extends XSuspendContext {
  private final @NotNull DartExecutionStack myExecutionStack;

  public DartSuspendContext(@NotNull final DartCommandLineDebugProcess debugProcess,
                            @NotNull final VmIsolate isolate,
                            @NotNull final List<VmCallFrame> vmCallFrames,
                            @Nullable final VmValue exception) {
    myExecutionStack = new DartExecutionStack(debugProcess, isolate, vmCallFrames, exception);
  }

  @Override
  @NotNull
  public XExecutionStack getActiveExecutionStack() {
    return myExecutionStack;
  }

  public void selectUpperNavigatableStackFrame(@NotNull final XDebugSession session) {
    final XStackFrame frameToStopAt = myExecutionStack.getFrameToStopAt();

    if (frameToStopAt != null && frameToStopAt != myExecutionStack.getTopFrame()) {
      session.addSessionListener(new XDebugSessionAdapter() {
        @Override
        public void stackFrameChanged() {
          if (session.getCurrentStackFrame() != frameToStopAt) {
            session.removeSessionListener(this);
            session.setCurrentStackFrame(myExecutionStack, frameToStopAt, false);
          }
        }
      });

      // The next call to setCurrentStackFrame() doesn't actually change frame, but without it stackFrameChanged() in listener is never called (see setCurrentStackFrame() implementation).
      // First time our listener (registered few lines above) is notified from our own setCurrentStackFrame() at the next line and listener doesn't do anything.
      // Second time listener is notified when UI is initialized and XFramesView tries to set top frame as current: this time our listener effectively changes current frame.
      session.setCurrentStackFrame(myExecutionStack, frameToStopAt, false);
    }
  }
}
