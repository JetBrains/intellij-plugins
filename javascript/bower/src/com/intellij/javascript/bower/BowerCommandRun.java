package com.intellij.javascript.bower;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.webcore.util.ProcessOutputCatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BowerCommandRun {

  private final ProcessOutputCatcher myCatcher;
  private final OSProcessHandler myProcessHandler;

  public BowerCommandRun(@NotNull OSProcessHandler processHandler) {
    myCatcher = new ProcessOutputCatcher(processHandler);
    myProcessHandler = processHandler;
  }

  public @NotNull ProcessOutput captureOutput(@Nullable ProgressIndicator indicator, long timeoutMillis) throws ExecutionException {
    ProcessOutput output = myCatcher.run(indicator, timeoutMillis);
    if (output.isTimeout()) {
      throw new ExecutionException(BowerBundle.message("dialog.message.command.timed.out", myProcessHandler.getCommandLineForLog()));
    }
    if (output.isCancelled()) {
      throw new ExecutionException(BowerBundle.message("dialog.message.command.cancelled", myProcessHandler.getCommandLineForLog()));
    }
    if (output.getExitCode() != 0) {
      throw new ExecutionException(
        BowerBundle
          .message("dialog.message.command.finished.with.exit.code", myProcessHandler.getCommandLineForLog(), output.getExitCode(),
                   output.getStdout(), output.getStderr()));
    }
    return output;
  }

  public void terminate() {
    myCatcher.terminateAndWait();
  }
}
