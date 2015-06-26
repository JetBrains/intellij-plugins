package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.*;
import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.execution.KarmaRunProfileState;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.NopProcessHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageProgramRunner extends GenericProgramRunner {

  private static final Logger LOG = Logger.getInstance(KarmaCoverageProgramRunner.class);
  private static final String COVERAGE_RUNNER_ID = KarmaCoverageProgramRunner.class.getSimpleName();

  @NotNull
  @Override
  public String getRunnerId() {
    return COVERAGE_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return CoverageExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof KarmaRunConfiguration;
  }

  @Override
  public RunnerSettings createConfigurationData(final ConfigurationInfoProvider settingsProvider) {
    return new CoverageRunnerData();
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    KarmaRunProfileState runProfileState = ObjectUtils.tryCast(state, KarmaRunProfileState.class);
    if (runProfileState == null) {
      return null;
    }
    final KarmaServer server = runProfileState.getServerOrStart(env.getExecutor());
    if (server == null) {
      return null;
    }
    KarmaCoveragePeer coveragePeer = getCoveragePeer(server);
    KarmaCoverageStartupStatus status = coveragePeer.getStartupStatus();
    if (status != null) {
      if (status.isSuccessful()) {
        return executeAfterSuccessfulInitialization(runProfileState, env, server);
      }
      return showWarningConsole(status, server, env);
    }
    coveragePeer.onCoverageInitialized(new KarmaCoverageInitializationCallback() {
      @Override
      public void onCoverageInitialized(@NotNull KarmaCoverageStartupStatus startupStatus) {
        RunnerAndConfigurationSettings configuration = env.getRunnerAndConfigurationSettings();
        if (configuration != null) {
          ProgramRunnerUtil.executeConfiguration(env, true, true);
        }
      }
    });
    return showWarningConsole(null, server, env);
  }

  @NotNull
  private static KarmaCoveragePeer getCoveragePeer(@NotNull KarmaServer server) {
    KarmaCoveragePeer coveragePeer = server.getCoveragePeer();
    if (coveragePeer == null) {
      throw new RuntimeException("Coverage peer should be initialized");
    }
    return coveragePeer;
  }

  private static RunContentDescriptor showWarningConsole(@Nullable KarmaCoverageStartupStatus status,
                                                         @NotNull KarmaServer server,
                                                         @NotNull ExecutionEnvironment env) {
    if (status != null && status.isKarmaCoveragePackageNeededToBeInstalled()) {
      server.getRestarter().requestRestart();
    }
    ExecutionConsole console = new KarmaCoverageConfigurationErrorConsole(env.getProject(), server, status);
    final ProcessHandler processHandler = new NopProcessHandler();
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void startNotified(ProcessEvent event) {
        processHandler.destroyProcess();
      }
    });
    DefaultExecutionResult executionResult = new DefaultExecutionResult(console, processHandler);
    RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, env);
    return contentBuilder.showRunContent(env.getContentToReuse());
  }

  @NotNull
  private static RunContentDescriptor executeAfterSuccessfulInitialization(@NotNull KarmaRunProfileState state,
                                                                           @NotNull ExecutionEnvironment env,
                                                                           @NotNull KarmaServer server) throws ExecutionException {
    ExecutionResult executionResult = state.executeWithServer(env.getExecutor(), server);
    if (server.areBrowsersReady()) {
      return doCoverage(executionResult, env, server);
    }
    RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, env);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(env.getContentToReuse());
    server.onBrowsersReady(new Runnable() {
      @Override
      public void run() {
        ExecutionUtil.restartIfActive(descriptor);
      }
    });
    return descriptor;
  }

  @NotNull
  private static RunContentDescriptor doCoverage(@NotNull ExecutionResult executionResult,
                                                 @NotNull final ExecutionEnvironment env,
                                                 @NotNull final KarmaServer server) {
    final KarmaRunConfiguration runConfiguration = (KarmaRunConfiguration) env.getRunProfile();
    CoverageEnabledConfiguration coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration);
    CoverageHelper.resetCoverageSuit(runConfiguration);
    final String coverageFilePath = coverageEnabledConfiguration.getCoverageFilePath();
    RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, env);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(env.getContentToReuse());
    if (coverageFilePath != null) {
      KarmaCoveragePeer coveragePeer = getCoveragePeer(server);
      coveragePeer.startCoverageSession(new KarmaCoverageSession() {
        @Override
        public void onCoverageSessionFinished(@NotNull final File lcovFile) {
          UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
              try {
                FileUtil.copy(lcovFile, new File(coverageFilePath));
              }
              catch (IOException e) {
                LOG.error("Can't copy files from " + lcovFile.getAbsolutePath() + " to " + coverageFilePath, e);
                return;
              }
              RunnerSettings runnerSettings = env.getRunnerSettings();
              if (runnerSettings != null) {
                KarmaCoverageRunner coverageRunner = KarmaCoverageRunner.getInstance();
                coverageRunner.setKarmaServer(server);
                CoverageDataManager.getInstance(env.getProject()).processGatheredCoverage(runConfiguration, runnerSettings);
              }
            }
          });
        }
      });
    }
    return descriptor;
  }

}
