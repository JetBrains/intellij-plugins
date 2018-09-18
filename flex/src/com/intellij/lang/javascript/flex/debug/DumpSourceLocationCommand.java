package com.intellij.lang.javascript.flex.debug;

import org.jetbrains.annotations.NonNls;

class DumpSourceLocationCommand extends DebuggerCommand {
  private final FlexDebugProcess myFlexDebugProcess;

  DumpSourceLocationCommand(FlexDebugProcess flexDebugProcess) {
    super("bt", CommandOutputProcessingType.SPECIAL_PROCESSING);
    myFlexDebugProcess = flexDebugProcess;
  }

  @Override
  CommandOutputProcessingMode onTextAvailable(@NonNls final String text) {
    if (!myFlexDebugProcess.getSession().isPaused()) {
      final String[] frames = FlexSuspendContext.splitStackFrames(text);
      if (frames.length > 0) {
        final FlexSuspendContext suspendContext = new FlexSuspendContext(myFlexDebugProcess, frames);
        myFlexDebugProcess.getSession().positionReached(suspendContext);
      }
    }

    return CommandOutputProcessingMode.DONE;
  }
}
