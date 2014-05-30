package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallFrame;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallback;
import com.jetbrains.lang.dart.ide.runner.server.google.VmResult;
import com.jetbrains.lang.dart.ide.runner.server.google.VmValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

class DartDebuggerEvaluator extends XDebuggerEvaluator {
  @NotNull private final DartCommandLineDebugProcess myDebugProcess;
  @NotNull private final VmCallFrame myVmCallFrame;

  public DartDebuggerEvaluator(final @NotNull DartCommandLineDebugProcess debugProcess, final @NotNull VmCallFrame vmCallFrame) {
    myDebugProcess = debugProcess;
    myVmCallFrame = vmCallFrame;
  }

  public boolean isCodeFragmentEvaluationSupported() {
    return false;
  }

  public void evaluate(@NotNull final String expression,
                       @NotNull final XEvaluationCallback callback,
                       @Nullable final XSourcePosition expressionPosition) {
    try {
      myDebugProcess.getVmConnection().evaluateOnCallFrame(myVmCallFrame.getIsolate(), myVmCallFrame, expression,
                                                           new VmCallback<VmValue>() {
                                                             public void handleResult(final VmResult<VmValue> result) {
                                                               if (result.isError()) {
                                                                 callback.errorOccurred(result.getError());
                                                               }
                                                               else {
                                                                 final VmValue vmValue = result.getResult();
                                                                 callback.evaluated(new DartValue(myDebugProcess, vmValue));
                                                               }
                                                             }
                                                           }
      );
    }
    catch (IOException e) {
      callback.errorOccurred(e.toString());
    }
  }
}
