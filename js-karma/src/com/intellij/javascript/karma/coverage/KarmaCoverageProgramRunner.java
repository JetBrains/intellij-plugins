package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageExecutor;
import com.intellij.coverage.CoverageHelper;
import com.intellij.coverage.CoverageRunnerData;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.*;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

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
  public RunnerSettings createConfigurationData(@NotNull final ConfigurationInfoProvider settingsProvider) {
    return new CoverageRunnerData();
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    ExecutionResult executionResult = state.execute(env.getExecutor(), this);
    if (executionResult == null) {
      return null;
    }
    RunContentDescriptor descriptor = KarmaUtil.createDefaultDescriptor(executionResult, env);
    KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult, state);
    if (consoleView == null) {
      return descriptor;
    }
    KarmaServer server = consoleView.getKarmaServer();
    if (executionResult.getProcessHandler() instanceof NopProcessHandler) {
      server.onBrowsersReady(() -> ExecutionUtil.restartIfActive(descriptor));
    }
    else {
      listenForCoverageFile(env, server);
    }
    return descriptor;
  }

  private static void listenForCoverageFile(@NotNull ExecutionEnvironment env, @NotNull KarmaServer server) {
    RunConfigurationBase<?> runConfiguration = (RunConfigurationBase<?>)env.getRunProfile();
    CoverageEnabledConfiguration coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration);
    CoverageHelper.resetCoverageSuit(runConfiguration);
    String coverageFilePath = coverageEnabledConfiguration.getCoverageFilePath();
    if (coverageFilePath != null) {
      KarmaCoveragePeer coveragePeer = server.getCoveragePeer();
      Objects.requireNonNull(coveragePeer);
      coveragePeer.startCoverageSession(new KarmaCoverageSession() {
        @Override
        public void onCoverageSessionFinished(@Nullable File lcovFile) {
          LOG.info("Processing karma coverage file: " + lcovFile);
          ReadAction.run(() -> {
            Project project = env.getProject();
            if (!project.isDisposed()) {
              if (lcovFile != null) {
                processLcovInfoFile(lcovFile, coverageFilePath, env, server, runConfiguration);
                return;
              }
              ApplicationManager.getApplication().invokeLater(() -> {
                int response = Messages.showYesNoDialog(project,
                                                        KarmaBundle.message("coverage.cannot_find_lcov.dialog.message"),
                                                        KarmaBundle.message("coverage.cannot_find_lcov.dialog.title"),
                                                        KarmaBundle.message("coverage.cannot_find_lcov.select_lcov.button", "lcov.info"),
                                                        KarmaBundle.message("coverage.cannot_find_lcov.cancel.button"),
                                                        Messages.getWarningIcon());
                if (response == Messages.YES) {
                  FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor(),
                                         project,
                                         null,
                                         null, file -> {
                      File selected = file != null ? VfsUtilCore.virtualToIoFile(file) : null;
                      if (selected != null) {
                        ApplicationManager.getApplication().executeOnPooledThread(() -> {
                          ReadAction.run(() -> {
                            if (!project.isDisposed()) {
                              processLcovInfoFile(selected, coverageFilePath, env, server, runConfiguration);
                            }
                          });
                        });
                      }
                    });
                }
              }, ModalityState.defaultModalityState());
            }
          });
        }
      });
    }
  }

  private static void processLcovInfoFile(@NotNull File lcovInfoFile,
                                          @NotNull String toCoverageFilePath,
                                          @NotNull ExecutionEnvironment env,
                                          @NotNull KarmaServer karmaServer,
                                          @NotNull RunConfigurationBase<?> runConfiguration) {
    try {
      FileUtil.copy(lcovInfoFile, new File(toCoverageFilePath));
    }
    catch (IOException e) {
      LOG.error("Cannot copy " + lcovInfoFile.getAbsolutePath() + " to " + toCoverageFilePath, e);
      return;
    }
    RunnerSettings runnerSettings = env.getRunnerSettings();
    if (runnerSettings != null) {
      KarmaCoverageRunner coverageRunner = KarmaCoverageRunner.getInstance();
      coverageRunner.setKarmaServer(karmaServer);
      CoverageDataManager.getInstance(env.getProject()).processGatheredCoverage(runConfiguration, runnerSettings);
    }
  }
}
