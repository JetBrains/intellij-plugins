// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.coverage;

import com.intellij.coverage.CoverageExecutor;
import com.intellij.coverage.CoverageHelper;
import com.intellij.coverage.CoverageRunnerData;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.process.*;
import com.intellij.execution.runners.DefaultProgramRunnerKt;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.actions.DartPubActionBase;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunningState;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartCoverageProgramRunner extends GenericProgramRunner {
  private static final Logger LOG = Logger.getInstance(DartCoverageProgramRunner.class.getName());

  private static final String ID = "DartCoverageProgramRunner";

  private boolean myCoveragePackageActivated;

  @Override
  public @NotNull String getRunnerId() {
    return ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return executorId.equals(CoverageExecutor.EXECUTOR_ID) && profile instanceof DartCommandLineRunConfiguration;
  }

  @Override
  public RunnerSettings createConfigurationData(final @NotNull ConfigurationInfoProvider settingsProvider) {
    return new CoverageRunnerData();
  }

  @Override
  protected @Nullable RunContentDescriptor doExecute(final @NotNull RunProfileState state,
                                                     final @NotNull ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final DartCommandLineRunConfiguration runConfiguration = (DartCommandLineRunConfiguration)env.getRunProfile();

    final DartSdk sdk = DartSdk.getDartSdk(runConfiguration.getProject());
    if (sdk == null) {
      throw new ExecutionException(DartBundle.message("dart.sdk.is.not.configured"));
    }

    final RunContentDescriptor result = DefaultProgramRunnerKt.executeState(state, env, this);
    if (result == null) {
      return null;
    }

    if (!myCoveragePackageActivated && !activateCoverage(runConfiguration.getProject(), sdk)) {
      throw new ExecutionException(DartBundle.message("dialog.message.cannot.activate.pub.package.coverage"));
    }

    final ProcessHandler dartAppProcessHandler = result.getProcessHandler();

    if (dartAppProcessHandler != null) {
      ((DartCommandLineRunningState)state)
        .addObservatoryUrlConsumer(observatoryUrl -> startCollectingCoverage(env, dartAppProcessHandler, observatoryUrl));
    }

    return result;
  }

  private static void startCollectingCoverage(final @NotNull ExecutionEnvironment env,
                                              final @NotNull ProcessHandler dartAppProcessHandler,
                                              final @NotNull String observatoryUrl) {
    final DartCommandLineRunConfiguration dartRC = (DartCommandLineRunConfiguration)env.getRunProfile();

    final DartCoverageEnabledConfiguration coverageConfiguration =
      (DartCoverageEnabledConfiguration)CoverageEnabledConfiguration.getOrCreate(dartRC);
    final String coverageFilePath = coverageConfiguration.getCoverageFilePath();

    final DartSdk sdk = DartSdk.getDartSdk(env.getProject());
    LOG.assertTrue(sdk != null);

    GeneralCommandLine commandLine = new GeneralCommandLine();
    DartPubActionBase.setupPubExePath(commandLine, sdk);
    commandLine.addParameters("global", "run", "coverage:collect_coverage",
                              "--uri", observatoryUrl,
                              "--out", coverageFilePath,
                              "--resume-isolates",
                              "--wait-paused");

    try {
      final ProcessHandler coverageProcess = new OSProcessHandler(commandLine);

      coverageProcess.addProcessListener(new ProcessListener() {
        @Override
        public void onTextAvailable(final @NotNull ProcessEvent event, final @NotNull Key outputType) {
          LOG.debug(event.getText());
        }
      });

      coverageProcess.startNotify();
      coverageConfiguration.setCoverageProcess(coverageProcess);
      CoverageHelper.attachToProcess(dartRC, dartAppProcessHandler, env.getRunnerSettings());
    }
    catch (ExecutionException e) {
      LOG.error(e);
    }
  }

  private boolean activateCoverage(@NotNull Project project, @NotNull DartSdk sdk) {
    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
      if (indicator != null) {
        indicator.setIndeterminate(true);
      }

      try {
        // 'pub global list' is fast, let's run it first to find out if coverage package is already activated.
        // Following 'pub global activate' is long and may be cancelled by user
        checkIfCoverageActivated(sdk);

        // run 'pub global activate' regardless of activation status, because it checks for the coverage package update
        GeneralCommandLine commandLine = new GeneralCommandLine().withRedirectErrorStream(true);
        DartPubActionBase.setupPubExePath(commandLine, sdk);
        commandLine.addParameters("global", "activate", "coverage");
        ProcessOutput activateOutput = new CapturingProcessHandler(commandLine)
          .runProcessWithProgressIndicator(ProgressManager.getInstance().getProgressIndicator());

        if (activateOutput.getExitCode() != 0) {
          LOG.warn("'pub global activate coverage' exit code: " + activateOutput.getExitCode() +
                   ", stdout:\n" + activateOutput.getStdout());
        }

        if (!myCoveragePackageActivated) {
          checkIfCoverageActivated(sdk);
        }
      }
      catch (ExecutionException e) {
        LOG.warn(e);
      }
    }, DartBundle.message("progress.title.activating.coverage.package"), true, project);

    // Even if 'pub global activate' process has been cancelled we can proceed if coverage already activated
    return myCoveragePackageActivated;
  }

  private void checkIfCoverageActivated(@NotNull DartSdk sdk) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine().withRedirectErrorStream(true);
    DartPubActionBase.setupPubExePath(commandLine, sdk);
    commandLine.addParameters("global", "list");
    final ProcessOutput listOutput = new CapturingProcessHandler(commandLine)
      .runProcessWithProgressIndicator(ProgressManager.getInstance().getProgressIndicator());

    final String listOutputStdout = listOutput.getStdout();
    if (listOutput.getExitCode() == 0) {
      if (listOutputStdout.startsWith("coverage ") || listOutputStdout.contains("\ncoverage ")) {
        myCoveragePackageActivated = true;
      }
    }
    else {
      LOG.warn("'pub global list' exit code: " + listOutput.getExitCode() + ", stdout:\n" + listOutputStdout);
    }
  }
}
