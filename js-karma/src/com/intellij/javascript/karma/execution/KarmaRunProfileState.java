package com.intellij.javascript.karma.execution;

import com.intellij.coverage.CoverageExecutor;
import com.intellij.execution.*;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.CatchingConsumer;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunProfileState implements RunProfileState {

  private final Project myProject;
  private final ExecutionEnvironment myExecutionEnvironment;
  private final String myNodeInterpreterPath;
  private final String myKarmaPackageDirPath;
  private final KarmaRunSettings myRunSettings;
  private final KarmaExecutionType myExecutionType;

  public KarmaRunProfileState(@NotNull Project project,
                              @NotNull ExecutionEnvironment executionEnvironment,
                              @NotNull String nodeInterpreterPath,
                              @NotNull String karmaPackageDirPath,
                              @NotNull KarmaRunSettings runSettings,
                              @NotNull Executor executor) {
    myProject = project;
    myExecutionEnvironment = executionEnvironment;
    myNodeInterpreterPath = nodeInterpreterPath;
    myKarmaPackageDirPath = karmaPackageDirPath;
    myRunSettings = runSettings;
    myExecutionType = findExecutionType(executor);
  }

  @Override
  @Nullable
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    KarmaServer server = getServerOrStart(executor);
    if (server != null) {
      return executeWithServer(executor, server);
    }
    return null;
  }

  @Nullable
  public KarmaServer getServerOrStart(@NotNull final Executor executor) throws ExecutionException {
    KarmaServerSettings serverSettings = new KarmaServerSettings.Builder()
      .setNodeInterpreterPath(myNodeInterpreterPath)
      .setKarmaPackageDirPath(myKarmaPackageDirPath)
      .setRunSettings(myRunSettings)
      .build();

    KarmaServerRegistry registry = KarmaServerRegistry.getInstance(myProject);
    KarmaServer server = registry.getServer(serverSettings);
    if (server != null && server.getRestarter().isRestartRequired()) {
      server.shutdownAsync();
      server = null;
    }
    if (server == null) {
      registry.startServer(
        serverSettings,
        new CatchingConsumer<KarmaServer, Exception>() {
          @Override
          public void consume(final Exception e) {
            showServerStartupError(e);
          }

          @Override
          public void consume(KarmaServer server) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
              @Override
              public void run() {
                RunnerAndConfigurationSettings configuration = myExecutionEnvironment.getRunnerAndConfigurationSettings();
                if (configuration != null) {
                  ProgramRunnerUtil.executeConfiguration(myProject, configuration, executor);
                }
              }
            });
          }
        }
      );
    }
    return server;
  }

  @NotNull
  public ExecutionResult executeWithServer(@NotNull Executor executor,
                                           @NotNull KarmaServer server) throws ExecutionException {
    server.getWatcher().flush();
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
    // TODO make smtRunnerConsoleView instance of LanguageConsoleView to make it more usage for debugging
    DefaultExecutionResult executionResult = new DefaultExecutionResult(smtRunnerConsoleView, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction(myExecutionEnvironment));
    return executionResult;
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

  private void showServerStartupError(@NotNull final Exception serverException) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        StringBuilder errorMessage = new StringBuilder("Karma server launching failed.");
        Throwable e = serverException;
        String prevMessage = null;
        while (e != null) {
          String message = e.getMessage();
          if (message != null && !message.equals(prevMessage)) {
            errorMessage.append("\n\nCaused by:\n");
            errorMessage.append(message);
            prevMessage = message;
          }
          e = e.getCause();
        }
        Messages.showErrorDialog(myProject,
                                 errorMessage.toString(),
                                 "Karma Server Launching");
      }
    });
  }

}
