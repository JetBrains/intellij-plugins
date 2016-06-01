package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.icons.AllIcons.Debugger.*;
import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.LOG;

public class DartExecutionStack extends XExecutionStack {
  @NotNull private final DartCommandLineDebugProcess myDebugProcess;
  @NotNull private final VmIsolate myIsolate;
  private List<DartStackFrame> myStackFrames;

  public DartExecutionStack(@NotNull final DartCommandLineDebugProcess debugProcess,
                            @NotNull final VmIsolate isolate,
                            @Nullable final List<VmCallFrame> vmCallFrames,
                            @Nullable VmValue exception) {
    // active stack has vmCallFrames != null, but debugProcess.isIsolateSuspended(isolate) returns false at this point
    // for non-active suspended stacks debugProcess.isIsolateSuspended(isolate) is true
    // if vmCallFrames is null and debugProcess.isIsolateSuspended(isolate) is false -> isolate is running
    super(vmCallFrames != null || debugProcess.isIsolateSuspended(isolate)
          ? DartBundle.message("isolate.0.suspended", String.valueOf(isolate.getId()))
          : DartBundle.message("isolate.0.running", String.valueOf(isolate.getId())),

          vmCallFrames != null ? ThreadCurrent
                               : debugProcess.isIsolateSuspended(isolate) ? ThreadAtBreakpoint
                                                                          : ThreadRunning);

    myDebugProcess = debugProcess;
    myIsolate = isolate;

    if (vmCallFrames != null) {
      myStackFrames = new ArrayList<DartStackFrame>(vmCallFrames.size());

      for (VmCallFrame vmCallFrame : vmCallFrames) {
        final DartStackFrame frame = new DartStackFrame(debugProcess, vmCallFrame, exception);
        myStackFrames.add(frame);

        if (!frame.isInDartSdkPatchFile()) {
          // exception (if any) is added to the frame where debugger stops and to the upper frames
          exception = null;
        }
      }
    }
  }

  @Override
  @Nullable
  public XStackFrame getTopFrame() {
    // engine calls getTopFrame for active execution stack only for which we have myStackFrames initialized in constructor
    return myStackFrames == null || myStackFrames.isEmpty() ? null : myStackFrames.get(0);
  }

  @Override
  public void computeStackFrames(final int firstFrameIndex, @NotNull final XStackFrameContainer container) {
    if (!myDebugProcess.isIsolateSuspended(myIsolate)) {
      container.addStackFrames(Collections.<XStackFrame>emptyList(), true);
      return;
    }

    if (myStackFrames == null) {
      myStackFrames = new ArrayList<DartStackFrame>();

      try {
        myDebugProcess.getVmConnection().getStackTrace(myIsolate, new VmCallback<List<VmCallFrame>>() {
          @Override
          public void handleResult(VmResult<List<VmCallFrame>> result) {
            if (result.isError()) {
              container.errorOccurred(result.getError());
            }
            else {
              for (VmCallFrame vmCallFrame : result.getResult()) {
                myStackFrames.add(new DartStackFrame(myDebugProcess, vmCallFrame, null));
              }

              final List<DartStackFrame> subList = firstFrameIndex == 0 ? myStackFrames
                                                                        : myStackFrames.subList(firstFrameIndex, myStackFrames.size());
              container.addStackFrames(subList, true);
            }
          }
        });
      }
      catch (IOException e) {
        container.addStackFrames(Collections.<XStackFrame>emptyList(), true);
        LOG.info(e);
      }
    }
    else {
      final List<DartStackFrame> subList = firstFrameIndex == 0 ? myStackFrames
                                                                : myStackFrames.subList(firstFrameIndex, myStackFrames.size());
      container.addStackFrames(subList, true);
    }
  }

  @Nullable
  public DartStackFrame getFrameToStopAt() {
    return ContainerUtil.find(myStackFrames, frame -> !frame.isInDartSdkPatchFile());
  }

  @NotNull
  public VmIsolate getIsolate() {
    return myIsolate;
  }
}
