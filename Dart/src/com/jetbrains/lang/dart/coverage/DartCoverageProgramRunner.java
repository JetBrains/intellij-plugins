/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunningState;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartCoverageProgramRunner extends GenericProgramRunner {
  private static final Logger LOG = Logger.getInstance(DartCoverageProgramRunner.class.getName());

  private static final String ID = "DartCoverageProgramRunner";

  private boolean myCoveragePackageActivated;

  @NotNull
  @Override
  public String getRunnerId() {
    return ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return executorId.equals(CoverageExecutor.EXECUTOR_ID) && profile instanceof DartCommandLineRunConfiguration;
  }

  @Override
  public RunnerSettings createConfigurationData(@NotNull final ConfigurationInfoProvider settingsProvider) {
    return new CoverageRunnerData();
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(final @NotNull RunProfileState state,
                                           final @NotNull ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final DartCommandLineRunConfiguration runConfiguration = (DartCommandLineRunConfiguration)env.getRunProfile();

    final DartSdk sdk = DartSdk.getDartSdk(runConfiguration.getProject());
    if (sdk == null) {
      throw new ExecutionException(DartBundle.message("dart.sdk.is.not.configured"));
    }

    final String dartPubPath = DartSdkUtil.getPubPath(sdk);

    final RunContentDescriptor result = DefaultProgramRunnerKt.executeState(state, env, this);
    if (result == null) {
      return null;
    }

    if (!myCoveragePackageActivated && !activateCoverage(runConfiguration.getProject(), dartPubPath)) {
      throw new ExecutionException("Cannot activate pub package 'coverage'.");
    }

    final ProcessHandler dartAppProcessHandler = result.getProcessHandler();

    if (dartAppProcessHandler != null) {
      ((DartCommandLineRunningState)state)
        .addObservatoryUrlConsumer(observatoryUrl -> startCollectingCoverage(env, dartAppProcessHandler, observatoryUrl));
    }

    return result;
  }

  private static void startCollectingCoverage(@NotNull final ExecutionEnvironment env,
                                              @NotNull final ProcessHandler dartAppProcessHandler,
                                              @NotNull final String observatoryUrl) {
    final DartCommandLineRunConfiguration dartRC = (DartCommandLineRunConfiguration)env.getRunProfile();

    final DartCoverageEnabledConfiguration coverageConfiguration =
      (DartCoverageEnabledConfiguration)CoverageEnabledConfiguration.getOrCreate(dartRC);
    final String coverageFilePath = coverageConfiguration.getCoverageFilePath();

    final DartSdk sdk = DartSdk.getDartSdk(env.getProject());
    LOG.assertTrue(sdk != null);

    final GeneralCommandLine cmdline = new GeneralCommandLine().withExePath(DartSdkUtil.getPubPath(sdk))
      .withParameters("global", "run", "coverage:collect_coverage",
                      "--uri", observatoryUrl,
                      "--out", coverageFilePath,
                      "--resume-isolates",
                      "--wait-paused");

    try {
      final ProcessHandler coverageProcess = new OSProcessHandler(cmdline);

      coverageProcess.addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(@NotNull final ProcessEvent event, @NotNull final Key outputType) {
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

  private boolean activateCoverage(@NotNull final Project project, @NotNull final String dartPubPath) {
    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
      if (indicator != null) {
        indicator.setIndeterminate(true);
      }

      try {
        // 'pub global list' is fast, let's run it first to find out if coverage package is already activated.
        // Following 'pub global activate' is long and may be cancelled by user
        checkIfCoverageActivated(dartPubPath);

        // run 'pub global activate' regardless of activation status, because it checks for the coverage package update
        final ProcessOutput activateOutput = new CapturingProcessHandler(
          new GeneralCommandLine().withExePath(dartPubPath).withParameters("global", "activate", "coverage").withRedirectErrorStream(true))
          .runProcessWithProgressIndicator(ProgressManager.getInstance().getProgressIndicator());

        if (activateOutput.getExitCode() != 0) {
          LOG.warn("'pub global activate coverage' exit code: " + activateOutput.getExitCode() +
                   ", stdout:\n" + activateOutput.getStdout());
        }

        if (!myCoveragePackageActivated) {
          checkIfCoverageActivated(dartPubPath);
        }
      }
      catch (ExecutionException e) {
        LOG.warn(e);
      }
    }, DartBundle.message("progress.title.activating.coverage.package"), true, project);

    // Even if 'pub global activate' process has been cancelled we can proceed if coverage already activated
    return myCoveragePackageActivated;
  }

  private void checkIfCoverageActivated(@NotNull final String dartPubPath) throws ExecutionException {
    final ProcessOutput listOutput = new CapturingProcessHandler(
      new GeneralCommandLine().withExePath(dartPubPath).withParameters("global", "list").withRedirectErrorStream(true))
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
