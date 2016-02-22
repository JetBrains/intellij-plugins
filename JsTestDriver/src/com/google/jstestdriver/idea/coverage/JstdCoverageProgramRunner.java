package com.google.jstestdriver.idea.coverage;

import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.google.jstestdriver.idea.execution.JstdRunProfileState;
import com.google.jstestdriver.idea.execution.NopProcessHandler;
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
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

public class JstdCoverageProgramRunner extends AsyncGenericProgramRunner {

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
  protected Promise<RunProfileStarter> prepare(@NotNull ExecutionEnvironment environment, @NotNull RunProfileState state) throws ExecutionException {
    JstdRunProfileState jstdState = JstdRunProfileState.cast(state);
    if (jstdState.getRunSettings().isExternalServerType()) {
      return Promise.resolve(new MyStarter(null));
    }
    JstdToolWindowManager jstdToolWindowManager = JstdToolWindowManager.getInstance(environment.getProject());
    jstdToolWindowManager.setAvailable(true);
    JstdServer server = JstdServerRegistry.getInstance().getServer();
    if (server != null && !server.isStopped()) {
      return Promise.resolve(new MyStarter(server));
    }
    return jstdToolWindowManager.restartServer()
      .then(server1 -> server1 != null ? new MyStarter(server1) : null);
  }

  public static class MyStarter extends RunProfileStarter {
    private final JstdServer myServer;

    public MyStarter(@Nullable JstdServer server) {
      myServer = server;
    }

    @Nullable
    @Override
    public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
      FileDocumentManager.getInstance().saveAllDocuments();
      JstdRunConfiguration runConfiguration = (JstdRunConfiguration) environment.getRunProfile();
      CoverageEnabledConfiguration coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration);
      String coverageFilePath = coverageEnabledConfiguration.getCoverageFilePath();
      JstdRunProfileState jstdState = new JstdRunProfileState(environment, runConfiguration.getRunSettings(), coverageFilePath);
      ExecutionResult executionResult = jstdState.executeWithServer(myServer);

      RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, environment);
      final RunContentDescriptor descriptor = contentBuilder.showRunContent(environment.getContentToReuse());
      ProcessHandler processHandler = executionResult.getProcessHandler();
      if (processHandler instanceof NopProcessHandler) {
        if (myServer != null) {
          myServer.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
            @Override
            public void onBrowserCaptured(@NotNull JstdBrowserInfo info) {
              ExecutionUtil.restartIfActive(descriptor);
              myServer.removeLifeCycleListener(this);
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
}
