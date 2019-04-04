// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.google.jstestdriver.idea.coverage;

import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.google.jstestdriver.idea.execution.JstdRunProfileState;
import com.google.jstestdriver.idea.server.JstdBrowserInfo;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowManager;
import com.intellij.coverage.CoverageExecutor;
import com.intellij.coverage.CoverageHelper;
import com.intellij.coverage.CoverageRunnerData;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

public class JstdCoverageProgramRunner extends AsyncProgramRunner {
  private static final String COVERAGE_RUNNER_ID = JstdCoverageProgramRunner.class.getSimpleName();

  @NotNull
  @Override
  public String getRunnerId() {
    return COVERAGE_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return CoverageExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof JstdRunConfiguration;
  }

  @Override
  public RunnerSettings createConfigurationData(final ConfigurationInfoProvider settingsProvider) {
    return new CoverageRunnerData();
  }

  @NotNull
  @Override
  protected Promise<RunContentDescriptor> execute(@NotNull ExecutionEnvironment environment, @NotNull RunProfileState state) throws ExecutionException {
    if (JstdRunProfileState.cast(state).getRunSettings().isExternalServerType()) {
      return Promises.resolvedPromise(null);
    }
    JstdToolWindowManager jstdToolWindowManager = JstdToolWindowManager.getInstance(environment.getProject());
    jstdToolWindowManager.setAvailable(true);
    JstdServer server = JstdServerRegistry.getInstance().getServer();
    if (server != null && !server.isStopped()) {
      return Promises.resolvedPromise(start(server, environment));
    }
    return jstdToolWindowManager.restartServer()
      .thenAsync(it -> {
        try {
          return it == null ? null : Promises.resolvedPromise(start(it, environment));
        }
        catch (ExecutionException e) {
          return Promises.rejectedPromise(e);
        }
      });
  }


  @Nullable
  private static RunContentDescriptor start(@Nullable JstdServer server, @NotNull ExecutionEnvironment environment) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    JstdRunConfiguration runConfiguration = (JstdRunConfiguration)environment.getRunProfile();
    CoverageEnabledConfiguration coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration);
    String coverageFilePath = coverageEnabledConfiguration.getCoverageFilePath();
    JstdRunProfileState jstdState = new JstdRunProfileState(environment, runConfiguration.getRunSettings(), coverageFilePath);
    ExecutionResult executionResult = jstdState.executeWithServer(server);

    RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, environment);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(environment.getContentToReuse());
    ProcessHandler processHandler = executionResult.getProcessHandler();
    if (processHandler instanceof NopProcessHandler) {
      if (server != null) {
        server.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
          @Override
          public void onBrowserCaptured(@NotNull JstdBrowserInfo info) {
            ExecutionUtil.restartIfActive(descriptor);
            server.removeLifeCycleListener(this);
          }
        }, contentBuilder);
      }
    }
    else {
      CoverageHelper.attachToProcess(runConfiguration, processHandler, environment.getRunnerSettings());
    }
    return descriptor;
  }
}
