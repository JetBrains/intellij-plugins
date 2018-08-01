package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.javascript.jest.JestUtil;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.karma.server.KarmaJsSourcesLocator;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerTerminatedListener;
import com.intellij.javascript.karma.tree.KarmaTestProxyFilterProvider;
import com.intellij.javascript.nodejs.NodeStackTraceFilter;
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructure;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.lang.javascript.ConsoleCommandLineFolder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.execution.ParametersListUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.LocalFileFinder;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class KarmaExecutionSession {

  private static final Logger LOG = Logger.getInstance(KarmaExecutionSession.class);

  private final Project myProject;
  private final KarmaRunConfiguration myRunConfiguration;
  private final Executor myExecutor;
  private final KarmaServer myKarmaServer;
  private final KarmaRunSettings myRunSettings;
  private final ProcessHandler myProcessHandler;
  private final KarmaExecutionType myExecutionType;
  private final SMTRunnerConsoleView mySmtConsoleView;
  private final ConsoleCommandLineFolder myFolder = new ConsoleCommandLineFolder("karma", "run");
  private final List<List<String>> myFailedTestNames;

  public KarmaExecutionSession(@NotNull Project project,
                               @NotNull KarmaRunConfiguration runConfiguration,
                               @NotNull Executor executor,
                               @NotNull KarmaServer karmaServer,
                               @NotNull KarmaRunSettings runSettings,
                               @NotNull KarmaExecutionType executionType,
                               @Nullable List<List<String>> failedTestNames) throws ExecutionException {
    myProject = project;
    myRunConfiguration = runConfiguration;
    myExecutor = executor;
    myKarmaServer = karmaServer;
    myRunSettings = runSettings;
    myExecutionType = executionType;
    myFailedTestNames = failedTestNames;
    myProcessHandler = createProcessHandler(karmaServer);
    mySmtConsoleView = createSMTRunnerConsoleView();
    if (!(myProcessHandler instanceof NopProcessHandler)) {
      // show test result notifications for real test runs only
      mySmtConsoleView.attachToProcess(myProcessHandler);
      myFolder.foldCommandLine(mySmtConsoleView, myProcessHandler);
    }
  }

  @NotNull
  private SMTRunnerConsoleView createSMTRunnerConsoleView() {
    KarmaTestProxyFilterProvider filterProvider = new KarmaTestProxyFilterProvider(myProject, myKarmaServer);
    KarmaConsoleProperties consoleProperties = new KarmaConsoleProperties(myRunConfiguration, myExecutor, filterProvider);
    consoleProperties.addStackTraceFilter(new NodeStackTraceFilter(
      myProject, myKarmaServer.getServerSettings().getWorkingDirectorySystemDependent())
    );
    KarmaConsoleView consoleView = new KarmaConsoleView(consoleProperties, myKarmaServer, myExecutionType, myProcessHandler);
    SMTestRunnerConnectionUtil.initConsoleView(consoleView, consoleProperties.getTestFrameworkName());
    return consoleView;
  }

  public boolean isDebug() {
    return myExecutionType == KarmaExecutionType.DEBUG;
  }

  @NotNull
  private ProcessHandler createProcessHandler(@NotNull final KarmaServer server) throws ExecutionException {
    ProcessHandler processHandler = null;
    if (isDebug()) {
      if (server.isPortBound()) {
        processHandler = createOSProcessHandler(server);
      }
    }
    else {
      if (server.areBrowsersReady()) {
        processHandler = createOSProcessHandler(server);
      }
    }
    if (processHandler == null) {
      processHandler = new NopProcessHandler();
    }
    terminateOnServerShutdown(server, processHandler);
    return processHandler;
  }

  private static void terminateOnServerShutdown(@NotNull KarmaServer server, @NotNull ProcessHandler processHandler) {
    KarmaServerTerminatedListener terminationCallback = new KarmaServerTerminatedListener() {
      @Override
      public void onTerminated(int exitCode) {
        ScriptRunnerUtil.terminateProcessHandler(processHandler, 2000, null);
      }
    };
    server.onTerminated(terminationCallback);
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(@NotNull ProcessEvent event) {
        server.removeTerminatedListener(terminationCallback);
      }
    });
  }

  @NotNull
  private OSProcessHandler createOSProcessHandler(@NotNull KarmaServer server) throws ExecutionException {
    NodeJsInterpreter interpreter = myRunSettings.getInterpreterRef().resolveNotNull(myProject);
    GeneralCommandLine commandLine = createCommandLine(interpreter, server);
    OSProcessHandler processHandler = new KillableColoredProcessHandler(commandLine);
    server.getRestarter().onRunnerExecutionStarted(processHandler);
    ProcessTerminatedListener.attach(processHandler);
    return processHandler;
  }

  @NotNull
  private GeneralCommandLine createCommandLine(@NotNull NodeJsInterpreter interpreter,
                                               @NotNull KarmaServer server) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setWorkDirectory(myRunSettings.getWorkingDirectorySystemDependent());
    commandLine.setCharset(CharsetToolkit.UTF8_CHARSET);
    List<String> nodeOptionList = ParametersListUtil.parse(myRunSettings.getNodeOptions().trim());
    commandLine.addParameters(nodeOptionList);
    //NodeCommandLineUtil.addNodeOptionsForDebugging(commandLine, Collections.emptyList(), 5858, false, interpreter, true);
    File clientAppFile = KarmaJsSourcesLocator.getInstance().getClientAppFile();
    commandLine.addParameter(clientAppFile.getAbsolutePath());
    commandLine.addParameter("--serverPort=" + server.getServerPort());
    KarmaConfig config = server.getKarmaConfig();
    if (config != null) {
      commandLine.addParameter("--protocol=" + config.getProtocol());
      commandLine.addParameter("--urlRoot=" + config.getUrlRoot());
    }
    if (isDebug()) {
      commandLine.addParameter("--debug=true");
    }
    String testNamesPattern = getTestNamesPattern();
    if (testNamesPattern != null) {
      commandLine.addParameter("--testName=" + testNamesPattern);
      myFolder.addLastParameterFrom(commandLine);
    }
    NodeCommandLineConfigurator.find(interpreter).configure(commandLine);
    return commandLine;
  }

  @Nullable
  private String getTestNamesPattern() throws ExecutionException {
    if (myFailedTestNames != null) {
      return getTestNamesPattern(myFailedTestNames, false);
    }
    if (myRunSettings.getScopeKind() == KarmaScopeKind.TEST_FILE) {
      List<String> topNames = findTopLevelSuiteNames(myProject, myRunSettings.getTestFileSystemIndependentPath());
      String testFileName = PathUtil.getFileName(myRunSettings.getTestFileSystemIndependentPath());
      if (topNames.isEmpty()) {
        throw new ExecutionException("No tests found in " + testFileName);
      }
      return getTestNamesPattern(ContainerUtil.map(topNames, name -> Collections.singletonList(name)), true);
    }
    if (myRunSettings.getScopeKind() == KarmaScopeKind.SUITE) {
      return getTestNamesPattern(Collections.singletonList(myRunSettings.getTestNames()), true);
    }
    if (myRunSettings.getScopeKind() == KarmaScopeKind.TEST) {
      return getTestNamesPattern(Collections.singletonList(myRunSettings.getTestNames()), false);
    }
    return null;
  }

  @NotNull
  private static String getTestNamesPattern(@NotNull List<List<String>> testNames, boolean suite) {
    List<String> patterns = ContainerUtil.map(testNames, testFqn -> {
      List<String> escaped = ContainerUtil.mapNotNull(testFqn, s -> JestUtil.escapeJavaScriptRegexp(s));
      return StringUtil.join(escaped, " ");
    });
    if (patterns.isEmpty()) {
      return "$^"; // matches nothing
    }
    String result;
    if (patterns.size() == 1) {
      result = patterns.get(0);
    }
    else {
      result = "(" + StringUtil.join(patterns, ")|(") + ")";
    }
    return "^" + result + (suite ? " " : "$");
  }

  private static List<String> findTopLevelSuiteNames(@NotNull Project project, @NotNull String testFilePath) throws ExecutionException {
    VirtualFile file = LocalFileFinder.findFile(testFilePath);
    if (file == null) {
      throw new ExecutionException("Cannot find test file by " + testFilePath);
    }
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    JSFile jsFile = ObjectUtils.tryCast(psiFile, JSFile.class);
    if (jsFile == null) {
      LOG.info("Not a JavaScript file " + testFilePath + ", " + (psiFile == null ? "null" : psiFile.getClass()));
      throw new ExecutionException("Not a JavaScript file: " + testFilePath);
    }
    JasmineFileStructure jasmine = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    List<String> elements = jasmine.getTopLevelElements();
    if (!elements.isEmpty()) {
      return elements;
    }
    QUnitFileStructure qunit = QUnitFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    elements = qunit.getTopLevelElements();
    if (!elements.isEmpty()) {
      return elements;
    }
    throw new ExecutionException("No tests found in " + testFilePath);
  }

  @NotNull
  public ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  @NotNull
  public SMTRunnerConsoleView getSmtConsoleView() {
    return mySmtConsoleView;
  }
}
