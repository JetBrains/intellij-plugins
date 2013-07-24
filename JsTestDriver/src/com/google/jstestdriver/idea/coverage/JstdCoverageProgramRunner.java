package com.google.jstestdriver.idea.coverage;

import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.google.jstestdriver.idea.execution.JstdRunConfigurationVerifier;
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
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class JstdCoverageProgramRunner extends GenericProgramRunner {

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

  @Override
  protected RunContentDescriptor doExecute(Project project,
                                           RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
    return executeWithCoverage(project, contentToReuse, env);
  }

  @Nullable
  private RunContentDescriptor executeWithCoverage(Project project,
                                                   RunContentDescriptor contentToReuse,
                                                   ExecutionEnvironment env) throws ExecutionException {
    JstdRunConfiguration runConfiguration = (JstdRunConfiguration) env.getRunProfile();
    JstdRunConfigurationVerifier.checkJstdServerAndBrowserEnvironment(project, runConfiguration.getRunSettings(), false);
    FileDocumentManager.getInstance().saveAllDocuments();
    CoverageEnabledConfiguration coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration);
    String coverageFilePath = coverageEnabledConfiguration.getCoverageFilePath();
    RunProfileState state = runConfiguration.getCoverageState(env, coverageFilePath);
    ExecutionResult executionResult = state.execute(env.getExecutor(), this);
    if (executionResult == null) {
      return null;
    }

    CoverageHelper.attachToProcess(runConfiguration, executionResult.getProcessHandler(), env.getRunnerSettings());

    final RunContentBuilder contentBuilder = new RunContentBuilder(this, executionResult, env);
    return contentBuilder.showRunContent(contentToReuse);
  }
}
