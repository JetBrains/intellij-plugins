package com.intellij.javascript.karma.execution;

import com.intellij.execution.*;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.process.OSProcessHandler;
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
import com.intellij.javascript.karma.util.StreamCommandListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testIntegration.TestLocationProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class KarmaTestRunnerState extends CommandLineState {

  private static final String FRAMEWORK_NAME = "KarmaJavaScriptTestRunner";

  private final Project myProject;
  private final ExecutionEnvironment myExecutionEnvironment;
  private final String myNodeInterpreterPath;
  private final String myKarmaPackageDir;
  private final KarmaRunSettings myRunSettings;

  public KarmaTestRunnerState(@NotNull Project project,
                              @NotNull ExecutionEnvironment executionEnvironment,
                              @NotNull String nodeInterpreterPath,
                              @NotNull String karmaPackageDir,
                              @NotNull KarmaRunSettings runSettings) {
    super(executionEnvironment);
    myProject = project;
    myExecutionEnvironment = executionEnvironment;
    myNodeInterpreterPath = nodeInterpreterPath;
    myKarmaPackageDir = karmaPackageDir;
    myRunSettings = runSettings;
  }

  @Override
  @NotNull
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    final ProcessHandler processHandler = startProcess();
    ConsoleView consoleView = createConsole(myProject, myExecutionEnvironment, executor);
    consoleView.attachToProcess(processHandler);

    File configurationFile = new File(myRunSettings.getConfigPath());
    KarmaServer server = KarmaServerRegistry.getServerByConfigurationFile(configurationFile);
    if (server == null) {
      try {
        server = new KarmaServer(new File(myNodeInterpreterPath), new File(myKarmaPackageDir), configurationFile);
        server.addListener(new KarmaServerListener() {
          @Override
          public void onReady(int webServerPort, int runnerPort) {
            @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
            PrintStream printStream = new PrintStream(processHandler.getProcessInput(), false);
            printStream.println("Runner port:" + runnerPort);
            printStream.flush();
          }
        });
      }
      catch (IOException e) {
        throw new ExecutionException(e);
      }
    }

    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction());
    return executionResult;
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
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    GeneralCommandLine commandLine = createCommandLine(myRunSettings);
    Process process = commandLine.createProcess();
    KarmaTestRunnerProcess karmaProcess = new KarmaTestRunnerProcess(process);
    karmaProcess.getInputStream().addListener(new StreamCommandListener() {
      @Override
      public void onCommand(@NotNull String commandName) {
        System.out.println("Command '" + commandName + "'");
      }
    });
    OSProcessHandler osProcessHandler = new OSProcessHandler(process, commandLine.getCommandLineString());
    ProcessTerminatedListener.attach(osProcessHandler);
    return osProcessHandler;
  }

  @NotNull
  private GeneralCommandLine createCommandLine(@NotNull KarmaRunSettings runSettings) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    File configFile = new File(runSettings.getConfigPath());
    // looks like it should work with any working directory
    commandLine.setWorkDirectory(configFile.getParentFile());
    commandLine.setExePath(myNodeInterpreterPath);
    try {
      File clientAppFile = KarmaJavaScriptSourcesLocator.getClientAppFile();
      commandLine.addParameter(clientAppFile.getAbsolutePath());
    }
    catch (IOException e) {
      throw new ExecutionException("Can't find karma client runner", e);
    }
    commandLine.addParameter(myKarmaPackageDir);
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
