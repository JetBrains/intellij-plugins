package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.runners.*;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KarmaRunProgramRunner extends GenericProgramRunner {

  @NotNull
  @Override
  public String getRunnerId() {
    return "KarmaJavaScriptTestRunnerRun";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof KarmaRunConfiguration;
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
    if (executionResult == null) {
      return null;
    }
    KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult, state);
    final RunContentDescriptor descriptor = KarmaUtil.createDefaultDescriptor(executionResult, environment);
    if (consoleView == null) {
      return descriptor;
    }

    if (executionResult.getProcessHandler() instanceof NopProcessHandler) {
      consoleView.getKarmaServer().onBrowsersReady(() -> ExecutionUtil.restartIfActive(descriptor));
    }
    else {
      RerunTestsNotification.showRerunNotification(environment.getContentToReuse(), executionResult.getExecutionConsole());
    }
    RerunTestsAction.register(descriptor);
    return descriptor;
  }
}
