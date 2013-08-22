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
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsoleEx;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.execution.KarmaRunProfileState;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.karma.util.NopProcessHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
  protected RunContentDescriptor doExecute(@NotNull final Project project,
                                           RunProfileState state,
                                           @Nullable RunContentDescriptor contentToReuse,
                                           @NotNull final ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    KarmaRunProfileState karmaState = ObjectUtils.tryCast(state, KarmaRunProfileState.class);
    if (karmaState == null) {
      return null;
    }
    final KarmaServer server = karmaState.getServerOrStart(env.getExecutor());
    if (server == null) {
      return null;
    }
    KarmaCoveragePeer coveragePeer = server.getCoveragePeer();
    KarmaCoverageStartupStatus status = coveragePeer.getStartupStatus();
    if (status != null) {
      if (isSuccessStatus(project, server, status)) {
        return executeAfterSuccessfulInitialization(project, karmaState, contentToReuse, env, server);
      }
      return showKarmaServerOnly(server, env, contentToReuse);
    }
    coveragePeer.doWhenCoverageInitialized(new KarmaCoverageInitializationListener() {
      @Override
      public void onCoverageInitialized(@NotNull KarmaCoverageStartupStatus initStatus) {
        RunnerAndConfigurationSettings configuration = env.getRunnerAndConfigurationSettings();
        if (configuration != null) {
          ProgramRunnerUtil.executeConfiguration(project, configuration, env.getExecutor());
        }
      }
    }, project);
    return null;
  }

  private static boolean isSuccessStatus(@NotNull Project project,
                                         @NotNull KarmaServer server,
                                         @NotNull KarmaCoverageStartupStatus status) {
    if (!status.isCoverageReporterSpecifiedInConfig()) {
      KarmaCoverageWarnings.warnAboutMissingCoverageReporterInConfigFile(project);
    }
    else if (!status.isCoverageReportFound()) {
      if (!status.isCoveragePluginInstalled()) {
        KarmaCoverageWarnings.suggestCoveragePluginInstallation(project, server.getKarmaPackageDir());
        server.getRestarter().requestRestart();
      }
      else {
        KarmaCoverageWarnings.warnAboutMissingCoveragePluginInConfigFile(project);
      }
    }
    else {
      return true;
    }
    return false;
  }

  private RunContentDescriptor showKarmaServerOnly(@NotNull final KarmaServer server,
                                                   @NotNull final ExecutionEnvironment env,
                                                   @Nullable RunContentDescriptor contentToReuse) {
    ServerOutputConsole console = new ServerOutputConsole(env.getProject(), server);
    final ProcessHandler processHandler = new NopProcessHandler();
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void startNotified(ProcessEvent event) {
        processHandler.destroyProcess();
      }
    });
    DefaultExecutionResult executionResult = new DefaultExecutionResult(console, processHandler);
    RunContentBuilder contentBuilder = new RunContentBuilder(this, executionResult, env);
    return contentBuilder.showRunContent(contentToReuse);
  }

  @NotNull
  private RunContentDescriptor executeAfterSuccessfulInitialization(@NotNull Project project,
                                                                    @NotNull KarmaRunProfileState state,
                                                                    @Nullable RunContentDescriptor contentToReuse,
                                                                    @NotNull ExecutionEnvironment env,
                                                                    @NotNull KarmaServer server) throws ExecutionException {
    ExecutionResult executionResult = state.executeWithServer(env.getExecutor(), server);
    if (server.areBrowsersReady()) {
      return doCoverage(project, executionResult, contentToReuse, env, server);
    }
    RunContentBuilder contentBuilder = new RunContentBuilder(this, executionResult, env);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);
    server.onBrowsersReady(new Runnable() {
      @Override
      public void run() {
        KarmaUtil.restart(descriptor);
      }
    });
    return descriptor;
  }

  @NotNull
  private RunContentDescriptor doCoverage(@NotNull final Project project,
                                          @NotNull ExecutionResult executionResult,
                                          RunContentDescriptor contentToReuse,
                                          @NotNull final ExecutionEnvironment env,
                                          @NotNull final KarmaServer karmaServer) {
    final KarmaRunConfiguration runConfiguration = (KarmaRunConfiguration) env.getRunProfile();
    CoverageEnabledConfiguration coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration);
    CoverageHelper.resetCoverageSuit(runConfiguration);
    final String coverageFilePath = coverageEnabledConfiguration.getCoverageFilePath();
    RunContentBuilder contentBuilder = new RunContentBuilder(this, executionResult, env);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);
    if (coverageFilePath != null) {
      KarmaCoveragePeer coveragePeer = karmaServer.getCoveragePeer();
      coveragePeer.startCoverageSession(new KarmaCoverageSession() {
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
                KarmaCoverageRunner coverageRunner = CoverageRunner.getInstance(KarmaCoverageRunner.class);
                coverageRunner.setKarmaServer(karmaServer);
                CoverageDataManager.getInstance(project).processGatheredCoverage(runConfiguration, runnerSettings);
              }
            }
          });
        }
      });
    }
    return descriptor;
  }

  private static class ServerOutputConsole implements ExecutionConsoleEx {

    private final Project myProject;
    private final KarmaServer myServer;

    private ServerOutputConsole(@NotNull Project project, @NotNull KarmaServer server) {
      myProject = project;
      myServer = server;
    }

    @Override
    public void buildUi(RunnerLayoutUi ui) {
      KarmaServerLogComponent logComponent = new KarmaServerLogComponent(myProject, myServer, this);
      logComponent.installOn(ui);
    }

    @Nullable
    @Override
    public String getExecutionConsoleId() {
      return null;
    }

    @Override
    public JComponent getComponent() {
      return null;
    }

    @Override
    public JComponent getPreferredFocusableComponent() {
      return null;
    }

    @Override
    public void dispose() {
    }
  }

}
