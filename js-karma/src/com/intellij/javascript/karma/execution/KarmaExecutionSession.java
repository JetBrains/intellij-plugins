// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.*;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.karma.server.KarmaJsSourcesLocator;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerTerminatedListener;
import com.intellij.javascript.nodejs.NodeStackTraceFilter;
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.testFramework.interfaces.mochaTdd.MochaTddFileStructure;
import com.intellij.javascript.testFramework.interfaces.mochaTdd.MochaTddFileStructureBuilder;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructure;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder;
import com.intellij.javascript.testFramework.jasmine.JasmineSpecStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.javascript.testFramework.util.JSTestNamePattern;
import com.intellij.javascript.testing.JSTestRunnerUtil;
import com.intellij.lang.javascript.ConsoleCommandLineFolder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    KarmaConsoleProperties consoleProperties = myRunConfiguration.createTestConsoleProperties(myExecutor, myKarmaServer);
    consoleProperties.addStackTraceFilter(new NodeStackTraceFilter(
      myProject, myKarmaServer.getServerSettings().getWorkingDirectorySystemDependent())
    );
    KarmaConsoleView consoleView = new KarmaConsoleView(consoleProperties, myKarmaServer, myExecutionType, myProcessHandler);
    for (Filter filter : consoleProperties.getStackTrackFilters()) {
      if (!(filter instanceof NodeStackTraceFilter)) {
        consoleView.addMessageFilter(filter);
      }
    }
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
    OSProcessHandler processHandler = new KillableColoredProcessHandler.Silent(commandLine);
    server.getRestarter().onRunnerExecutionStarted(processHandler);
    ProcessTerminatedListener.attach(processHandler);
    return processHandler;
  }

  @NotNull
  private GeneralCommandLine createCommandLine(@NotNull NodeJsInterpreter interpreter,
                                               @NotNull KarmaServer server) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setWorkDirectory(myRunSettings.getWorkingDirectorySystemDependent());
    commandLine.setCharset(StandardCharsets.UTF_8);
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
      commandLine.addParameter("--testNamePattern=" + testNamesPattern);
      myFolder.addLastParameterFrom(commandLine);
    }
    NodeCommandLineConfigurator.find(interpreter).configure(commandLine, NodeCommandLineConfigurator.defaultOptions(myProject));
    return commandLine;
  }

  @Nullable
  private String getTestNamesPattern() throws ExecutionException {
    if (myFailedTestNames != null) {
      return JSTestRunnerUtil.getTestsPattern(myFailedTestNames, false);
    }
    if (myRunSettings.getScopeKind() == KarmaScopeKind.TEST_FILE) {
      List<List<JSTestNamePattern>> allFileTests = findAllFileTests(myProject, myRunSettings.getTestFileSystemIndependentPath());
      String testFileName = PathUtil.getFileName(myRunSettings.getTestFileSystemIndependentPath());
      if (allFileTests.isEmpty()) {
        throw new ExecutionException(KarmaBundle.message("execution.no_tests_found_in_file.dialog.message", testFileName));
      }
      return JSTestRunnerUtil.getTestNamesPattern(allFileTests, false);
    }
    if (myRunSettings.getScopeKind() == KarmaScopeKind.SUITE || myRunSettings.getScopeKind() == KarmaScopeKind.TEST) {
      return JSTestRunnerUtil.buildTestNamesPattern(myProject,
                                                    myRunSettings.getTestFileSystemDependentPath(),
                                                    myRunSettings.getTestNames(),
                                                    myRunSettings.getScopeKind() == KarmaScopeKind.SUITE);
    }
    return null;
  }

  private static @NotNull List<List<JSTestNamePattern>> findAllFileTests(@NotNull Project project,
                                                                         @NotNull String testFilePath) throws ExecutionException {
    VirtualFile file = LocalFileFinder.findFile(testFilePath);
    if (file == null) {
      throw new ExecutionException(KarmaBundle.message("execution.cannot_find_test_by_path.dialog.message", testFilePath));
    }
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    JSFile jsFile = ObjectUtils.tryCast(psiFile, JSFile.class);
    if (jsFile == null) {
      LOG.info("Not a JavaScript file " + testFilePath + ", " + (psiFile == null ? "null" : psiFile.getClass()));
      throw new ExecutionException(KarmaBundle.message("execution.javascript_file_expected.dialog.message", testFilePath));
    }
    List<List<JSTestNamePattern>> allTestsPatterns = new ArrayList<>(collectJasmineTests(jsFile));
    if (!allTestsPatterns.isEmpty()) {
      return allTestsPatterns;
    }
    MochaTddFileStructure mochaTdd = MochaTddFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    mochaTdd.forEachTest(test -> {
      allTestsPatterns.add(test.getTestTreePathPatterns());
    });
    if (!allTestsPatterns.isEmpty()) {
      return allTestsPatterns;
    }
    QUnitFileStructure qunit = QUnitFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    qunit.forEachTest(test -> {
      allTestsPatterns.add(ContainerUtil.newArrayList(JSTestNamePattern.literalPattern(test.getModuleStructure().getName()),
                                                      JSTestNamePattern.literalPattern(test.getName())));
    });
    if (!allTestsPatterns.isEmpty()) {
      return allTestsPatterns;
    }
    throw new ExecutionException(KarmaBundle.message("execution.no_tests_found_in_file.dialog.message", testFilePath));
  }

  private static @NotNull List<List<JSTestNamePattern>> collectJasmineTests(@NotNull JSFile jsFile) {
    JasmineFileStructure jasmine = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    return ContainerUtil.map(jasmine.getChildren(), element -> {
      List<JSTestNamePattern> patterns = element.getTestTreePathPatterns();
      if (element instanceof JasmineSpecStructure) {
        return patterns;
      }
      JSTestNamePattern anyTestPattern = new JSTestNamePattern(Collections.singletonList(JSTestNamePattern.anyRange("match all descendant suites/specs")));
      return ContainerUtil.concat(patterns, Collections.singletonList(anyTestPattern));
    });
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
