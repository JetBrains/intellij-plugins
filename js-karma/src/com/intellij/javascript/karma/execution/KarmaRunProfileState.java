// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.javascript.debugger.locationResolving.JSLocationResolver;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerRegistry;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.CatchingConsumer;
import com.intellij.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KarmaRunProfileState implements RunProfileState {

  private static final Logger LOG = Logger.getInstance(KarmaRunProfileState.class);

  private final Project myProject;
  private final KarmaRunConfiguration myRunConfiguration;
  private final ExecutionEnvironment myEnvironment;
  private final NodePackage myKarmaPackage;
  private final KarmaRunSettings myRunSettings;
  private final KarmaExecutionType myExecutionType;
  private List<List<String>> myFailedTestNames;

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
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
    KarmaServer server = getServerOrStart(executor);
    if (server != null) {
      return executeWithServer(executor, server);
    }
    return null;
  }

  @Nullable
  public KarmaServer getServerOrStart(@NotNull final Executor executor) throws ExecutionException {
    NodeJsInterpreter interpreter = myRunSettings.getInterpreterRef().resolveNotNull(myProject);
    KarmaServerSettings serverSettings = new KarmaServerSettings.Builder()
      .setNodeInterpreter(interpreter)
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
      JSLocationResolver locationResolver = ApplicationManager.getApplication().getService(JSLocationResolver.class);
      if (locationResolver != null) {
        // dependency is optional
        locationResolver.dropCache(myRunConfiguration);
      }
      registry.startServer(
        serverSettings,
        new CatchingConsumer<>() {
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
                                                              myExecutionType,
                                                              myFailedTestNames);
    SMTRunnerConsoleView consoleView = session.getSmtConsoleView();
    ProcessHandler processHandler = session.getProcessHandler();
    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
    executionResult.setRestartActions(((KarmaConsoleProperties)consoleView.getProperties()).createRerunFailedTestsAction(consoleView),
                                      new ToggleAutoTestAction());
    return executionResult;
  }

  public void setFailedTestNames(@NotNull List<List<String>> failedTestNames) {
    myFailedTestNames = failedTestNames;
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

  private void showServerStartupError(@NotNull Exception serverException) {
    Messages.showErrorDialog(myProject,
                             KarmaBundle.message("karma.server.launching.failed", ExceptionUtil.getMessage(serverException)),
                             KarmaBundle.message("karma.server.tab.title"));
  }

}
