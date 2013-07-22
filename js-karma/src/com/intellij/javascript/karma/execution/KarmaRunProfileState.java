package com.intellij.javascript.karma.execution;

import com.intellij.coverage.CoverageExecutor;
import com.intellij.execution.*;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunProfileState implements RunProfileState {

  private final Project myProject;
  private final ExecutionEnvironment myExecutionEnvironment;
  private final String myNodeInterpreterPath;
  private final String myKarmaPackageDir;
  private final KarmaRunSettings myRunSettings;
  private final KarmaExecutionType myExecutionType;

  public KarmaRunProfileState(@NotNull Project project,
                              @NotNull ExecutionEnvironment executionEnvironment,
                              @NotNull String nodeInterpreterPath,
                              @NotNull String karmaPackageDir,
                              @NotNull KarmaRunSettings runSettings,
                              @NotNull Executor executor) {
    myProject = project;
    myExecutionEnvironment = executionEnvironment;
    myNodeInterpreterPath = nodeInterpreterPath;
    myKarmaPackageDir = karmaPackageDir;
    myRunSettings = runSettings;
    myExecutionType = findExecutionType(executor);
  }

  @NotNull
  private static KarmaExecutionType findExecutionType(@NotNull Executor executor) {
    if (executor.equals(DefaultDebugExecutor.getDebugExecutorInstance())) {
      return KarmaExecutionType.DEBUG;
    }
    if (executor.equals(ExecutorRegistry.getInstance().getExecutorById(CoverageExecutor.EXECUTOR_ID))) {
      return KarmaExecutionType.COVERAGE;
    }
    return KarmaExecutionType.RUN;
  }

  @Override
  @NotNull
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    File configurationFile = new File(myRunSettings.getConfigPath());
    KarmaServer server = KarmaServerRegistry.getServerByConfigurationFile(configurationFile);
    if (server == null) {
      try {
        server = new KarmaServer(new File(myNodeInterpreterPath), new File(myKarmaPackageDir), configurationFile);
        KarmaServerRegistry.registerServer(server);
      }
      catch (IOException e) {
        throw new ExecutionException(e);
      }
    }
    KarmaExecutionSession session = new KarmaExecutionSession(myProject,
                                                              myExecutionEnvironment,
                                                              executor,
                                                              server,
                                                              myNodeInterpreterPath,
                                                              myRunSettings,
                                                              myExecutionType);
    SMTRunnerConsoleView smtRunnerConsoleView = session.getSmtConsoleView();
    Disposer.register(myProject, smtRunnerConsoleView);

    ProcessHandler processHandler = session.getProcessHandler();
    DefaultExecutionResult executionResult = new DefaultExecutionResult(smtRunnerConsoleView, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction());
    return executionResult;
  }

  @Override
  public RunnerSettings getRunnerSettings() {
    return myExecutionEnvironment.getRunnerSettings();
  }
}
