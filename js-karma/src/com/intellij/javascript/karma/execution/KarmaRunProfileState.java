// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.execution;

import com.intellij.coverage.CoverageExecutor;
import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.javascript.debugger.CommandLineDebugConfigurator;
import com.intellij.javascript.debugger.locationResolving.JSLocationResolver;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerRegistry;
import com.intellij.javascript.nodejs.debug.NodeDebuggableRunProfileState;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

import java.util.List;

public final class KarmaRunProfileState implements NodeDebuggableRunProfileState {

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
  public @NotNull Promise<ExecutionResult> execute(@Nullable CommandLineDebugConfigurator configurator) {
    try {
      return getServerOrStart().thenAsync(server -> {
        try {
          return executeWithServer(server);
        }
        catch (ExecutionException e) {
          return Promises.rejectedPromise(e);
        }
      });
    }
    catch (ExecutionException e) {
      return Promises.rejectedPromise(e);
    }
  }

  public @NotNull Promise<KarmaServer> getServerOrStart() throws ExecutionException {
    NodeJsInterpreter interpreter = myRunSettings.getInterpreterRef().resolveNotNull(myProject);
    KarmaServerSettings serverSettings = new KarmaServerSettings(myEnvironment.getExecutor(),
                                                                 interpreter,
                                                                 myKarmaPackage,
                                                                 myRunSettings,
                                                                 myRunConfiguration);

    KarmaServerRegistry registry = KarmaServerRegistry.getInstance(myProject);
    KarmaServer server = registry.getServer(serverSettings);
    if (server != null && server.getRestarter().isRestartRequired()) {
      server.shutdownAsync();
      server = null;
    }
    if (server != null) {
      return Promises.resolvedPromise(server);
    }
    JSLocationResolver locationResolver = ApplicationManager.getApplication().getService(JSLocationResolver.class);
    if (locationResolver != null) {
      // dependency is optional
      locationResolver.dropCache(myRunConfiguration);
    }
    return registry.startServer(serverSettings);
  }

  private @NotNull Promise<ExecutionResult> executeWithServer(@NotNull KarmaServer server) throws ExecutionException {
    return executeUnderProgress(new KarmaExecutionSession(myProject,
                                                          myRunConfiguration,
                                                          myEnvironment.getExecutor(),
                                                          server,
                                                          myRunSettings,
                                                          myExecutionType,
                                                          myFailedTestNames));
  }

  private @NotNull Promise<ExecutionResult> executeUnderProgress(@NotNull KarmaExecutionSession session) {
    AsyncPromise<ExecutionResult> promise = new AsyncPromise<>();
    String title = JavaScriptBundle.message("node.execution.starting.process.progress.title", myRunConfiguration.getName());
    ProgressManager.getInstance().run(new Task.Backgroundable(myProject, title, true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        runInBackground(promise, session);
      }
    });
    return promise;
  }

  private static void runInBackground(@NotNull AsyncPromise<ExecutionResult> promise, @NotNull KarmaExecutionSession session) {
    try {
      ProcessHandler processHandler = session.createProcessHandler();
      ApplicationManager.getApplication().invokeLater(() -> {
        promise.setResult(createExecutionResult(session, processHandler));
      });
    }
    catch (Exception e) {
      promise.setError(e);
    }
  }

  private static @NotNull ExecutionResult createExecutionResult(@NotNull KarmaExecutionSession session,
                                                                @NotNull ProcessHandler processHandler) {
    SMTRunnerConsoleView consoleView = session.createSMTRunnerConsoleView(processHandler);
    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
    executionResult.setRestartActions(((KarmaConsoleProperties)consoleView.getProperties()).createRerunFailedTestsAction(consoleView),
                                      new ToggleAutoTestAction());
    if (!(processHandler instanceof NopProcessHandler)) {
      // show test result notifications for real test runs only
      consoleView.attachToProcess(processHandler);
      session.getFolder().foldCommandLine(consoleView, processHandler);
    }
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
}
