package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import org.jetbrains.annotations.NotNull;

public class DartCommandLineRunner extends DefaultProgramRunner {

  @NotNull
  @Override
  public String getRunnerId() {
    return "DartCommandLineRunner";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof DartCommandLineRunConfiguration;
  }
}
