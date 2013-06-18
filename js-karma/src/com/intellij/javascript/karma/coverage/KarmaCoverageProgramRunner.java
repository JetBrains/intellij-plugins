package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.*;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageProgramRunner extends GenericProgramRunner {

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

  @SuppressWarnings({"deprecation", "UnnecessaryFullyQualifiedName"})
  @Override
  public com.intellij.openapi.util.JDOMExternalizable createConfigurationData(final ConfigurationInfoProvider settingsProvider) {
    return new CoverageRunnerData();
  }

  @Override
  protected RunContentDescriptor doExecute(Project project,
                                           Executor executor,
                                           RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    ExecutionResult executionResult = state.execute(executor, this);
    if (executionResult == null) {
      return null;
    }
    KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult);
    if (consoleView == null) {
      throw new RuntimeException("KarmaConsoleView was expected!");
    }
    final KarmaServer karmaServer = consoleView.getKarmaExecutionSession().getKarmaServer();
    if (karmaServer.isReady() && karmaServer.hasCapturedBrowsers()) {
      return doCoverage(project, executor, executionResult, contentToReuse, env, karmaServer);
    }
    RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor, executionResult, env);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);
    karmaServer.doWhenReadyWithCapturedBrowser(new Runnable() {
      @Override
      public void run() {
        descriptor.getRestarter().run();
      }
    });
    return descriptor;
  }

  @NotNull
  private RunContentDescriptor doCoverage(@NotNull final Project project,
                                          @NotNull Executor executor,
                                          @NotNull ExecutionResult executionResult,
                                          RunContentDescriptor contentToReuse,
                                          @NotNull final ExecutionEnvironment env,
                                          @NotNull final KarmaServer karmaServer) {
    final KarmaRunConfiguration runConfiguration = (KarmaRunConfiguration) env.getRunProfile();
    CoverageEnabledConfiguration coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration);
    CoverageHelper.resetCoverageSuit(runConfiguration);
    String coverageFilePath = coverageEnabledConfiguration.getCoverageFilePath();
    if (coverageFilePath != null) {
      karmaServer.startCoverageSession(new KarmaCoverageSession(coverageFilePath) {
        @Override
        public void onCoverageSessionFinished() {
          UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
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
    final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor, executionResult, env);
    return contentBuilder.showRunContent(contentToReuse);
  }

}
