package com.intellij.lang.javascript.flex.debug;

import org.jetbrains.annotations.NonNls;

class DumpSourceLocationCommand extends DebuggerCommand {
  private FlexDebugProcess myFlexDebugProcess;

  public DumpSourceLocationCommand(FlexDebugProcess flexDebugProcess) {
    super("frame 0", CommandOutputProcessingType.SPECIAL_PROCESSING);
    myFlexDebugProcess = flexDebugProcess;
  }

  @Override
  CommandOutputProcessingMode onTextAvailable(@NonNls final String text) {
    if (!myFlexDebugProcess.getSession().isPaused()) {
      final FlexStackFrame topFrame = FlexSuspendContext.getStackFrame(myFlexDebugProcess, text);
      assert topFrame != null;
      final FlexSuspendContext suspendContext = new FlexSuspendContext(topFrame);
      myFlexDebugProcess.getSession().positionReached(suspendContext);
    }

    return CommandOutputProcessingMode.DONE;
  }
}
