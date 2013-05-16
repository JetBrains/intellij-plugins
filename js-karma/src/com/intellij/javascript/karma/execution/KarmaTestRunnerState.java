package com.intellij.javascript.karma.execution;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class KarmaTestRunnerState implements RunProfileState {

  private final Project myProject;
  private final ExecutionEnvironment myExecutionEnvironment;
  private final String myNodeInterpreterPath;
  private final String myKarmaPackageDir;
  private final KarmaRunSettings myRunSettings;
  private KarmaServer myKarmaServer;

  public KarmaTestRunnerState(@NotNull Project project,
                              @NotNull ExecutionEnvironment executionEnvironment,
                              @NotNull String nodeInterpreterPath,
                              @NotNull String karmaPackageDir,
                              @NotNull KarmaRunSettings runSettings) {
    myProject = project;
    myExecutionEnvironment = executionEnvironment;
    myNodeInterpreterPath = nodeInterpreterPath;
    myKarmaPackageDir = karmaPackageDir;
    myRunSettings = runSettings;
  }

  @Override
  @NotNull
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    File configurationFile = new File(myRunSettings.getConfigPath());
    KarmaServer server = KarmaServerRegistry.getServerByConfigurationFile(configurationFile);
    if (server == null) {
      try {
        server = new KarmaServer(new File(myNodeInterpreterPath), new File(myKarmaPackageDir), configurationFile);
        KarmaServerRegistry.registerServer(server);
      }
      catch (IOException e) {
        throw new ExecutionException(e);
      }
    }
    myKarmaServer = server;

    KarmaTestTreeConsole testTreeConsole = new KarmaTestTreeConsole(myExecutionEnvironment,
                                                                    executor,
                                                                    myKarmaServer,
                                                                    myNodeInterpreterPath,
                                                                    myRunSettings);
    Disposer.register(myProject, testTreeConsole);

    ProcessHandler processHandler = testTreeConsole.getProcessHandler();
    DefaultExecutionResult executionResult = new DefaultExecutionResult(testTreeConsole, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction());
    return executionResult;
  }

  public KarmaServer getKarmaServer() {
    return myKarmaServer;
  }

  @Override
  public RunnerSettings getRunnerSettings() {
    return myExecutionEnvironment.getRunnerSettings();
  }

  @Override
  public ConfigurationPerRunnerSettings getConfigurationSettings() {
    return myExecutionEnvironment.getConfigurationSettings();
  }


}
