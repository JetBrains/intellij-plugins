package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.google.gson.JsonObject;
import com.intellij.util.io.socketConnection.AbstractResponseToRequestHandler;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.connection.JsonResponse;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartExecutionStack extends XExecutionStack {
  private final DartCommandLineDebugProcess myDebugProcess;
  private List<DartStackFrame> myStackFrames = null;

  public DartExecutionStack(@NotNull DartCommandLineDebugProcess debugProcess) {
    this(debugProcess, new ArrayList<DartStackFrame>());
  }

  public DartExecutionStack(@NotNull DartCommandLineDebugProcess debugProcess, List<DartStackFrame> stackFrames) {
    super("DartVM");
    myDebugProcess = debugProcess;
    myStackFrames = stackFrames;
  }

  @Override
  public XStackFrame getTopFrame() {
    return myStackFrames.isEmpty() ? null : myStackFrames.get(0);
  }

  @Override
  public void computeStackFrames(final int firstFrameIndex, final XStackFrameContainer container) {
    if (!myStackFrames.isEmpty()) {
      addStackFrames(container, firstFrameIndex - 1);
      return;
    }
    myDebugProcess.sendSimpleCommand("getStackTrace", new AbstractResponseToRequestHandler<JsonResponse>() {
      @Override
      public boolean processResponse(JsonResponse response) {
        if (response.getJsonObject().get("error") == null) {
          final JsonObject result = response.getJsonObject().getAsJsonObject("result");
          myStackFrames.clear();
          myStackFrames.addAll(DartStackFrame.fromJson(myDebugProcess, result.getAsJsonArray("callFrames")));
          DartStackFrame.requestLines(myDebugProcess, myStackFrames, null);
        }
        addStackFrames(container, firstFrameIndex - 1);
        return true;
      }
    });
  }

  private void addStackFrames(XStackFrameContainer container, int firstFrameIndex) {
    final List<DartStackFrame> frames = myStackFrames.size() < 2
                                        ? Collections.<DartStackFrame>emptyList()
                                        : myStackFrames.subList(firstFrameIndex, myStackFrames.size());
    container.addStackFrames(frames, true);
  }
}
