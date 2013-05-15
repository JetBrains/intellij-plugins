package com.intellij.javascript.karma.execution;

import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsoleEx;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerListener;
import com.intellij.javascript.karma.server.KarmaServerRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testIntegration.TestLocationProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

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
    ApplicationManager.getApplication().assertIsDispatchThread();
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

    KarmaTestTreeConsole testTreeConsole = new KarmaTestTreeConsole(myProject,
                                                                    myExecutionEnvironment,
                                                                    executor,
                                                                    myKarmaServer,
                                                                    myNodeInterpreterPath,
                                                                    myRunSettings);

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
