package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.Location;
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
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.ExecutionConsoleEx;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerAdapter;
import com.intellij.javascript.karma.server.KarmaServerListener;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.javascript.karma.tree.KarmaProxyPrinterProvider;
import com.intellij.javascript.karma.util.NopProcessHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testIntegration.TestLocationProvider;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class KarmaTestRunConsole implements ExecutionConsoleEx {

  private static final Logger LOG = Logger.getInstance(KarmaTestRunConsole.class);
  private static final String FRAMEWORK_NAME = "KarmaJavaScriptTestRunner";

  private final Project myProject;
  private final ExecutionEnvironment myEnvironment;
  private final Executor myExecutor;
  private final KarmaServer myKarmaServer;
  private final String myNodeInterpreterPath;
  private final KarmaRunSettings myRunSettings;
  private final SMTRunnerConsoleView mySmtConsoleView;
  private final ProcessHandler myProcessHandler;
  private RunContentDescriptor myRunContentDescriptor;

  public KarmaTestRunConsole(@NotNull Project project,
                             @NotNull ExecutionEnvironment environment,
                             @NotNull Executor executor,
                             @NotNull KarmaServer karmaServer,
                             @NotNull String nodeInterpreterPath,
                             @NotNull KarmaRunSettings runSettings) throws ExecutionException {
    myProject = project;
    myEnvironment = environment;
    myExecutor = executor;
    myKarmaServer = karmaServer;
    myNodeInterpreterPath = nodeInterpreterPath;
    myRunSettings = runSettings;
    mySmtConsoleView = createSMTRunnerConsoleView();
    myProcessHandler = createProcessHandler(karmaServer);
  }

  @Override
  public void buildUi(final RunnerLayoutUi ui) {
    boolean serverReady = myKarmaServer.isReady();
    final JComponent component, preferredFocusableComponent;
    if (serverReady) {
      component = mySmtConsoleView.getComponent();
      preferredFocusableComponent = mySmtConsoleView.getPreferredFocusableComponent();
    }
    else {
      component = createStubComponent();
      preferredFocusableComponent = null;
    }
    final Content consoleContent = ui.createContent(ExecutionConsole.CONSOLE_CONTENT_ID,
                                                    component,
                                                    "Test Run",
                                                    AllIcons.Debugger.Console,
                                                    preferredFocusableComponent);
    consoleContent.setCloseable(false);
    ui.addContent(consoleContent, 0, PlaceInGrid.bottom, false);
    if (serverReady) {
      ui.selectAndFocus(consoleContent, false, false);
    }

    if (!serverReady) {
      myKarmaServer.addListener(new KarmaServerAdapter() {
        @Override
        public void onReady(int webServerPort, int runnerPort) {
          ui.removeContent(consoleContent, false);
          consoleContent.setComponent(mySmtConsoleView.getComponent());
          consoleContent.setPreferredFocusableComponent(mySmtConsoleView.getPreferredFocusableComponent());
          ui.addContent(consoleContent, 0, PlaceInGrid.bottom, false);
          ui.selectAndFocus(consoleContent, false, false);
        }
      });
    }

    KarmaServerLogComponent logComponent = new KarmaServerLogComponent(myProject, myKarmaServer);
    logComponent.installOn(ui);
  }

  @Nullable
  @Override
  public String getExecutionConsoleId() {
    return null;
  }

  @Override
  public JComponent getComponent() {
    return mySmtConsoleView.getComponent();
  }

  @Override
  public JComponent getPreferredFocusableComponent() {
    return mySmtConsoleView.getPreferredFocusableComponent();
  }

  @Override
  public void dispose() {
    Disposer.dispose(mySmtConsoleView);
  }

  @NotNull
  private static JComponent createStubComponent() {
    JLabel label = new JLabel("Karma server is not ready", SwingConstants.CENTER);
    label.setOpaque(true);
    Color treeBg = UIManager.getColor("Tree.background");
    label.setBackground(ColorUtil.toAlpha(treeBg, 180));
    return label;
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

    KarmaProxyPrinterProvider printerProvider = new KarmaProxyPrinterProvider(myKarmaServer);
    SMTRunnerConsoleView consoleView = SMTestRunnerConnectionUtil.createConsoleWithCustomLocator(
      FRAMEWORK_NAME,
      testConsoleProperties,
      myEnvironment.getRunnerSettings(),
      myEnvironment.getConfigurationSettings(),
      new KarmaTestLocationProvider(),
      true,
      printerProvider
    );
    printerProvider.setConsoleView(consoleView);
    return consoleView;
  }

  @NotNull
  private ProcessHandler createProcessHandler(@NotNull KarmaServer server) throws ExecutionException {
    final File clientAppFile;
    try {
      clientAppFile = server.getClientAppFile();
    }
    catch (IOException e) {
      throw new ExecutionException("Can't find karma-intellij test runner", e);
    }
    if (server.isReady()) {
      int runnerPort = server.getRunnerPort();
      return createOSProcessHandler(runnerPort, clientAppFile);
    }
    final NopProcessHandler nopProcessHandler = new NopProcessHandler();
    server.addListener(new KarmaServerListener() {
      @Override
      public void onReady(int webServerPort, int runnerPort) {
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
    commandLine.addParameter("--runnerPort=" + runnerPort);
    return commandLine;
  }

  @NotNull
  public ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  public void setRunContentDescriptor(@NotNull RunContentDescriptor descriptor) {
    myRunContentDescriptor = descriptor;
  }

  private static class KarmaTestLocationProvider implements TestLocationProvider {
    @NotNull
    @Override
    public List<Location> getLocation(@NotNull String protocolId, @NotNull String locationData, Project project) {
      return Collections.emptyList();
    }
  }

}
