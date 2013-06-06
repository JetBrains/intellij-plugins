package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerAdapter;
import com.intellij.javascript.karma.tree.KarmaTestProxyFilterProvider;
import com.intellij.javascript.karma.util.NopProcessHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class KarmaExecutionSession {

  private static final Logger LOG = Logger.getInstance(KarmaExecutionSession.class);
  private static final String FRAMEWORK_NAME = "KarmaJavaScriptTestRunner";

  private final Project myProject;
  private final ExecutionEnvironment myEnvironment;
  private final Executor myExecutor;
  private final KarmaServer myKarmaServer;
  private final String myNodeInterpreterPath;
  private final KarmaRunSettings myRunSettings;
  private final SMTRunnerConsoleView mySmtConsoleView;
  private final ProcessHandler myProcessHandler;
  private final boolean myDebug;
  private RunContentDescriptor myRunContentDescriptor;

  public KarmaExecutionSession(@NotNull Project project,
                               @NotNull ExecutionEnvironment environment,
                               @NotNull Executor executor,
                               @NotNull KarmaServer karmaServer,
                               @NotNull String nodeInterpreterPath,
                               @NotNull KarmaRunSettings runSettings,
                               boolean debug) throws ExecutionException {
    myProject = project;
    myEnvironment = environment;
    myExecutor = executor;
    myKarmaServer = karmaServer;
    myNodeInterpreterPath = nodeInterpreterPath;
    myRunSettings = runSettings;
    mySmtConsoleView = createSMTRunnerConsoleView();
    myDebug = debug;
    myProcessHandler = createProcessHandler(karmaServer);
  }

  @NotNull
  private SMTRunnerConsoleView createSMTRunnerConsoleView() {
    KarmaRunConfiguration runConfiguration = (KarmaRunConfiguration) myEnvironment.getRunProfile();
    TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(
      new RuntimeConfigurationProducer.DelegatingRuntimeConfiguration<KarmaRunConfiguration>(runConfiguration),
      FRAMEWORK_NAME,
      myExecutor
    );
    testConsoleProperties.setUsePredefinedMessageFilter(false);
    testConsoleProperties.setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);

    KarmaConsoleView consoleView = new KarmaConsoleView(testConsoleProperties,
                                                        myEnvironment,
                                                        SMTestRunnerConnectionUtil.getSplitterPropertyName(FRAMEWORK_NAME),
                                                        myKarmaServer,
                                                        this);
    Disposer.register(myProject, consoleView);
    SMTestRunnerConnectionUtil.initConsoleView(consoleView,
                                               FRAMEWORK_NAME,
                                               new KarmaTestLocationProvider(myProject),
                                               true,
                                               new KarmaTestProxyFilterProvider(myProject, myKarmaServer));
    return consoleView;
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
    if (server.isReady()) {
      int runnerPort = server.getRunnerPort();
      return createOSProcessHandler(runnerPort, clientAppFile);
    }
    final NopProcessHandler nopProcessHandler = new NopProcessHandler();
    server.addListener(new KarmaServerAdapter() {
      @Override
      public void onReady(int webServerPort, final int runnerPort) {
        server.doWhenReadyWithCapturedBrowser(new Runnable() {
          @Override
          public void run() {
            try {
              OSProcessHandler osProcessHandler = createOSProcessHandler(runnerPort, clientAppFile);
              if (myRunContentDescriptor != null) {
                myRunContentDescriptor.setProcessHandler(osProcessHandler);
              }
              osProcessHandler.startNotify();
            }
            catch (ExecutionException e) {
              LOG.warn(e);
              // TODO handle
            }
          }
        });
      }

      @Override
      public void onTerminated(int exitCode) {
        nopProcessHandler.destroyProcess();
      }
    });
    return nopProcessHandler;
  }

  @NotNull
  private OSProcessHandler createOSProcessHandler(int runnerPort, @NotNull File clientAppFile) throws ExecutionException {
    GeneralCommandLine commandLine = createCommandLine(runnerPort, clientAppFile);
    Process process = commandLine.createProcess();
    OSProcessHandler osProcessHandler = new KillableColoredProcessHandler(process, commandLine.getCommandLineString());
    ProcessTerminatedListener.attach(osProcessHandler);
    mySmtConsoleView.attachToProcess(osProcessHandler);
    return osProcessHandler;
  }

  @NotNull
  private GeneralCommandLine createCommandLine(int runnerPort, @NotNull File clientAppFile) {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    File configFile = new File(myRunSettings.getConfigPath());
    // looks like it should work with any working directory
    commandLine.setWorkDirectory(configFile.getParentFile());
    commandLine.setExePath(myNodeInterpreterPath);
    //commandLine.addParameter("--debug-brk=5858");
    commandLine.addParameter(clientAppFile.getAbsolutePath());
    commandLine.addParameter("--karmaPackageDir=" + myKarmaServer.getKarmaJsSourcesLocator().getKarmaPackageDir());
    commandLine.addParameter("--runnerPort=" + runnerPort);
    if (myDebug) {
      commandLine.addParameter("--debug=true");
    }
    return commandLine;
  }

  @NotNull
  public ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  public void setRunContentDescriptor(@NotNull RunContentDescriptor descriptor) {
    myRunContentDescriptor = descriptor;
  }

  @NotNull
  public KarmaServer getKarmaServer() {
    return myKarmaServer;
  }

  @NotNull
  public SMTRunnerConsoleView getSmtConsoleView() {
    return mySmtConsoleView;
  }
}
