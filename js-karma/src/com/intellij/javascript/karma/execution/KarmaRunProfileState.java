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
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.CatchingConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KarmaRunProfileState implements RunProfileState {

  private static final Logger LOG = Logger.getInstance(KarmaRunProfileState.class);

  private final Project myProject;
  private final KarmaRunConfiguration myRunConfiguration;
  private final ExecutionEnvironment myEnvironment;
  private final NodePackage myKarmaPackage;
  private final KarmaRunSettings myRunSettings;
  private final KarmaExecutionType myExecutionType;

  public KarmaRunProfileState(@NotNull Project project,
                              @NotNull KarmaRunConfiguration runConfiguration,
                              @NotNull ExecutionEnvironment environment,
                              @NotNull NodePackage karmaPackage) {
    myProject = project;
    myRunConfiguration = runConfiguration;
    myEnvironment = environment;
    myKarmaPackage = karmaPackage;
    myRunSettings = runConfiguration.getRunSettings();
    myExecutionType = findExecutionType(myEnvironment.getExecutor());
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
    NodeJsInterpreter interpreter = myRunSettings.getInterpreterRef().resolve(myProject);
    NodeJsLocalInterpreter localInterpreter = NodeJsLocalInterpreter.castAndValidate(interpreter);
    KarmaServerSettings serverSettings = new KarmaServerSettings.Builder()
      .setNodeInterpreter(localInterpreter)
      .setKarmaPackage(myKarmaPackage)
      .setRunSettings(myRunSettings)
      .setWithCoverage(myExecutionType == KarmaExecutionType.COVERAGE)
      .setDebug(myExecutionType == KarmaExecutionType.DEBUG)
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
          public void consume(KarmaServer server) {
            RunnerAndConfigurationSettings configuration = myEnvironment.getRunnerAndConfigurationSettings();
            if (configuration != null) {
              ProgramRunnerUtil.executeConfiguration(configuration, executor);
            }
          }

          @Override
          public void consume(final Exception e) {
            LOG.error(e);
            showServerStartupError(e);
          }
        }
      );
    }
    return server;
  }

  @NotNull
  public ExecutionResult executeWithServer(@NotNull Executor executor,
                                           @NotNull KarmaServer server) throws ExecutionException {
    KarmaExecutionSession session = new KarmaExecutionSession(myProject,
                                                              myRunConfiguration,
                                                              executor,
                                                              server,
                                                              myRunSettings,
                                                              myExecutionType);
    SMTRunnerConsoleView smtRunnerConsoleView = session.getSmtConsoleView();
    ProcessHandler processHandler = session.getProcessHandler();
    // TODO make smtRunnerConsoleView instance of LanguageConsoleView to make it more usage for debugging
    DefaultExecutionResult executionResult = new DefaultExecutionResult(smtRunnerConsoleView, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction());
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
    StringBuilder errorMessage = new StringBuilder("Karma server launching failed");
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
                             "Karma Server");
  }

}
