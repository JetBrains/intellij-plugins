// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.debug;

import org.jetbrains.annotations.NonNls;

class DumpSourceLocationCommand extends DebuggerCommand {
  private final FlexDebugProcess myFlexDebugProcess;

  DumpSourceLocationCommand(FlexDebugProcess flexDebugProcess) {
    super("bt", CommandOutputProcessingType.SPECIAL_PROCESSING);
    myFlexDebugProcess = flexDebugProcess;
  }

  @Override
  CommandOutputProcessingMode onTextAvailable(final @NonNls String text) {
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
