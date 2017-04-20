package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.TestProxyFilterProvider;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerTerminatedListener;
import com.intellij.javascript.karma.tree.KarmaTestProxyFilterProvider;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.CharsetToolkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class KarmaExecutionSession {

  private static final String FRAMEWORK_NAME = "KarmaJavaScriptTestRunner";

  private final Project myProject;
  private final KarmaRunConfiguration myRunConfiguration;
  private final Executor myExecutor;
  private final KarmaServer myKarmaServer;
  private final KarmaRunSettings myRunSettings;
  private final SMTRunnerConsoleView mySmtConsoleView;
  private final ProcessHandler myProcessHandler;
  private final KarmaExecutionType myExecutionType;

  public KarmaExecutionSession(@NotNull Project project,
                               @NotNull KarmaRunConfiguration runConfiguration,
                               @NotNull Executor executor,
                               @NotNull KarmaServer karmaServer,
                               @NotNull KarmaRunSettings runSettings,
                               @NotNull KarmaExecutionType executionType) throws ExecutionException {
    myProject = project;
    myRunConfiguration = runConfiguration;
    myExecutor = executor;
    myKarmaServer = karmaServer;
    myRunSettings = runSettings;
    mySmtConsoleView = createSMTRunnerConsoleView();
    myExecutionType = executionType;
    myProcessHandler = createProcessHandler(karmaServer);
  }

  @NotNull
  private SMTRunnerConsoleView createSMTRunnerConsoleView() {
    KarmaTestProxyFilterProvider filterProvider = new KarmaTestProxyFilterProvider(myProject, myKarmaServer);
    TestConsoleProperties testConsoleProperties = new KarmaConsoleProperties(myRunConfiguration, myExecutor, filterProvider);
    KarmaConsoleView consoleView = new KarmaConsoleView(testConsoleProperties, myKarmaServer, this);
    Disposer.register(myProject, consoleView);
    SMTestRunnerConnectionUtil.initConsoleView(consoleView, FRAMEWORK_NAME);
    return consoleView;
  }

  public boolean isDebug() {
    return myExecutionType == KarmaExecutionType.DEBUG;
  }

  @NotNull
  private ProcessHandler createProcessHandler(@NotNull final KarmaServer server) throws ExecutionException {
    final File clientAppFile;
    try {
      clientAppFile = server.getKarmaJsSourcesLocator().getClientAppFile();
    }
    catch (IOException e) {
      throw new ExecutionException("Can't find karma-intellij test runner", e);
    }
    if (server.areBrowsersReady()) {
      return createOSProcessHandler(server, clientAppFile);
    }
    final NopProcessHandler nopProcessHandler = new NopProcessHandler();
    terminateOnServerShutdown(server, nopProcessHandler);
    return nopProcessHandler;
  }

  private static void terminateOnServerShutdown(@NotNull final KarmaServer server, @NotNull final ProcessHandler processHandler) {
    final KarmaServerTerminatedListener terminationCallback = new KarmaServerTerminatedListener() {
      @Override
      public void onTerminated(int exitCode) {
        processHandler.destroyProcess();
      }
    };
    server.onTerminated(terminationCallback);
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        server.removeTerminatedListener(terminationCallback);
      }
    });
  }

  @NotNull
  private OSProcessHandler createOSProcessHandler(@NotNull KarmaServer server,
                                                  @NotNull File clientAppFile) throws ExecutionException {
    NodeJsLocalInterpreter interpreter = myRunSettings.getInterpreterRef().resolveAsLocal(myProject);
    GeneralCommandLine commandLine = createCommandLine(interpreter, server.getServerPort(), server.getKarmaConfig(), clientAppFile);
    OSProcessHandler processHandler = new KillableColoredProcessHandler(commandLine);
    server.getRestarter().onRunnerExecutionStarted(processHandler);
    ProcessTerminatedListener.attach(processHandler);
    mySmtConsoleView.attachToProcess(processHandler);
    return processHandler;
  }

  @NotNull
  private GeneralCommandLine createCommandLine(@NotNull NodeJsLocalInterpreter interpreter,
                                               int serverPort,
                                               @Nullable KarmaConfig config,
                                               @NotNull File clientAppFile) {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    File configFile = new File(myRunSettings.getConfigPath());
    // looks like it should work with any working directory
    commandLine.setWorkDirectory(configFile.getParentFile());
    commandLine.setCharset(CharsetToolkit.UTF8_CHARSET);
    commandLine.setExePath(interpreter.getInterpreterSystemDependentPath());
    //NodeCommandLineUtil.addNodeOptionsForDebugging(commandLine, Collections.emptyList(), 5858, false, interpreter, true);
    commandLine.addParameter(clientAppFile.getAbsolutePath());
    commandLine.addParameter("--karmaPackageDir=" + myKarmaServer.getKarmaJsSourcesLocator().getKarmaPackageDir());
    commandLine.addParameter("--serverPort=" + serverPort);
    if (config != null) {
      commandLine.addParameter("--urlRoot=" + config.getUrlRoot());
    }
    if (isDebug()) {
      commandLine.addParameter("--debug=true");
    }
    return commandLine;
  }

  @NotNull
  public ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  @NotNull
  public KarmaServer getKarmaServer() {
    return myKarmaServer;
  }

  @NotNull
  public SMTRunnerConsoleView getSmtConsoleView() {
    return mySmtConsoleView;
  }

  private static class KarmaConsoleProperties extends SMTRunnerConsoleProperties {
    private final KarmaTestProxyFilterProvider myFilterProvider;

    public KarmaConsoleProperties(KarmaRunConfiguration configuration, Executor executor, KarmaTestProxyFilterProvider filterProvider) {
      super(configuration, FRAMEWORK_NAME, executor);
      myFilterProvider = filterProvider;
      setUsePredefinedMessageFilter(true);
      setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);
      setIfUndefined(TestConsoleProperties.HIDE_IGNORED_TEST, true);
      setIfUndefined(TestConsoleProperties.SCROLL_TO_SOURCE, true);
      setIfUndefined(TestConsoleProperties.SELECT_FIRST_DEFECT, true);
      setIdBasedTestTree(true);
      setPrintTestingStartedTime(false);
    }

    @Override
    public SMTestLocator getTestLocator() {
      return KarmaTestLocationProvider.INSTANCE;
    }

    @Override
    public TestProxyFilterProvider getFilterProvider() {
      return myFilterProvider;
    }
  }
}
