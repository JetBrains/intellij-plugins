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

  private static final String FRAMEWORK_NAME = "KarmaJavaScriptTestRunner";

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
    ProcessHandler processHandler = startProcess(server);
    ConsoleView consoleView = createConsole(myProject, myExecutionEnvironment, executor);
    consoleView.attachToProcess(processHandler);

    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
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

  private static ConsoleView createConsole(@NotNull Project project,
                                           @NotNull ExecutionEnvironment env,
                                           Executor executor)
    throws ExecutionException {
    KarmaRunConfiguration runConfiguration = (KarmaRunConfiguration) env.getRunProfile();
    TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(
      new RuntimeConfigurationProducer.DelegatingRuntimeConfiguration<KarmaRunConfiguration>(runConfiguration),
      FRAMEWORK_NAME,
      executor
    );
    testConsoleProperties.setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);

    SMTRunnerConsoleView smtConsoleView = SMTestRunnerConnectionUtil.createConsoleWithCustomLocator(
      FRAMEWORK_NAME,
      testConsoleProperties,
      env.getRunnerSettings(),
      env.getConfigurationSettings(),
      new KarmaTestLocationProvider(),
      true,
      null
    );

    Disposer.register(project, smtConsoleView);
    return smtConsoleView;
  }

  @NotNull
  private ProcessHandler startProcess(@NotNull KarmaServer server) throws ExecutionException {
    int runnerPort = -1;
    if (server.isReady()) {
      runnerPort = server.getRunnerPort();
    }
    File clientAppFile;
    try {
      clientAppFile = server.getClientAppFile();
    }
    catch (IOException e) {
      throw new ExecutionException("Can't find karma-intellij test runner", e);
    }
    GeneralCommandLine commandLine = createCommandLine(runnerPort, clientAppFile);
    Process process = commandLine.createProcess();
    final ProcessHandler processHandler = new KillableColoredProcessHandler(process, commandLine.getCommandLineString());
    ProcessTerminatedListener.attach(processHandler);
    if (runnerPort == -1) {
      server.addListener(new KarmaServerListener() {
        @Override
        public void onReady(int webServerPort, int runnerPort) {
          @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
          PrintWriter pw = new PrintWriter(processHandler.getProcessInput(), false);
          pw.print("runner port " + runnerPort + "\n");
          pw.flush();
        }
      });
    }
    return processHandler;
  }

  @NotNull
  private GeneralCommandLine createCommandLine(int runnerPort, @NotNull File clientAppFile) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    File configFile = new File(myRunSettings.getConfigPath());
    // looks like it should work with any working directory
    commandLine.setWorkDirectory(configFile.getParentFile());
    commandLine.setExePath(myNodeInterpreterPath);
    //commandLine.addParameter("--debug-brk=5858");
    commandLine.addParameter(clientAppFile.getAbsolutePath());
    commandLine.addParameter("--karmaPackageDir=" + myKarmaPackageDir);
    if (runnerPort != -1) {
      commandLine.addParameter("--runnerPort=" + String.valueOf(runnerPort));
    }
    return commandLine;
  }

  private static class KarmaTestLocationProvider implements TestLocationProvider {
    @NotNull
    @Override
    public List<Location> getLocation(@NotNull String protocolId, @NotNull String locationData, Project project) {
      return Collections.emptyList();
    }
  }

}
