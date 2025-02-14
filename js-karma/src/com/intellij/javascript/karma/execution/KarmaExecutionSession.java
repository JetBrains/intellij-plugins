// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.*;
import com.intellij.execution.target.ResolvedPortBinding;
import com.intellij.execution.target.TargetedCommandLineBuilder;
import com.intellij.execution.target.value.TargetValue;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.karma.server.KarmaJsSourcesLocator;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerTerminatedListener;
import com.intellij.javascript.nodejs.NodeStackTraceFilter;
import com.intellij.javascript.nodejs.execution.NodeTargetRun;
import com.intellij.javascript.nodejs.execution.NodeTargetRunOptions;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.remote.NodeJsRemoteInterpreter;
import com.intellij.javascript.testFramework.util.JSTestNamePattern;
import com.intellij.javascript.testing.JSTestRunnerUtil;
import com.intellij.lang.javascript.ConsoleCommandLineFolder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.io.LocalFileFinder;

import java.nio.file.Path;
import java.util.List;

import static com.intellij.javascript.nodejs.execution.NodeTargetRunOptions.shouldUsePtyForTestRunners;

public class KarmaExecutionSession {

  private static final Logger LOG = Logger.getInstance(KarmaExecutionSession.class);

  private final Project myProject;
  private final KarmaRunConfiguration myRunConfiguration;
  private final Executor myExecutor;
  private final KarmaServer myKarmaServer;
  private final KarmaRunSettings myRunSettings;
  private final KarmaExecutionType myExecutionType;
  private final ConsoleCommandLineFolder myFolder = new ConsoleCommandLineFolder("karma", "run");
  private final List<List<String>> myFailedTestNames;

  public KarmaExecutionSession(@NotNull Project project,
                               @NotNull KarmaRunConfiguration runConfiguration,
                               @NotNull Executor executor,
                               @NotNull KarmaServer karmaServer,
                               @NotNull KarmaRunSettings runSettings,
                               @NotNull KarmaExecutionType executionType,
                               @Nullable List<List<String>> failedTestNames) {
    myProject = project;
    myRunConfiguration = runConfiguration;
    myExecutor = executor;
    myKarmaServer = karmaServer;
    myRunSettings = runSettings;
    myExecutionType = executionType;
    myFailedTestNames = failedTestNames;
  }

  public @NotNull SMTRunnerConsoleView createSMTRunnerConsoleView(@NotNull ProcessHandler processHandler) {
    KarmaConsoleProperties consoleProperties = myRunConfiguration.createTestConsoleProperties(myExecutor, myKarmaServer);
    consoleProperties.addStackTraceFilter(new NodeStackTraceFilter(
      myProject, myKarmaServer.getServerSettings().getWorkingDirectorySystemDependent())
    );
    KarmaConsoleView consoleView = new KarmaConsoleView(consoleProperties, myKarmaServer, myExecutionType, processHandler);
    for (Filter filter : consoleProperties.getStackTrackFilters()) {
      if (!(filter instanceof NodeStackTraceFilter)) {
        consoleView.addMessageFilter(filter);
      }
    }
    SMTestRunnerConnectionUtil.initConsoleView(consoleView, consoleProperties.getTestFrameworkName());
    return consoleView;
  }

  private boolean isDebug() {
    return myExecutionType == KarmaExecutionType.DEBUG;
  }

  public @NotNull ConsoleCommandLineFolder getFolder() {
    return myFolder;
  }

  public @NotNull ProcessHandler createProcessHandler() throws ExecutionException {
    ProcessHandler processHandler = null;
    if (isDebug()) {
      if (myKarmaServer.isPortBound()) {
        processHandler = createOSProcessHandler(myKarmaServer);
      }
    }
    else {
      if (myKarmaServer.areBrowsersReady()) {
        processHandler = createOSProcessHandler(myKarmaServer);
      }
    }
    if (processHandler == null) {
      processHandler = new NopProcessHandler();
    }
    terminateOnServerShutdown(myKarmaServer, processHandler);
    return processHandler;
  }

  private static void terminateOnServerShutdown(@NotNull KarmaServer server, @NotNull ProcessHandler processHandler) {
    KarmaServerTerminatedListener terminationCallback = new KarmaServerTerminatedListener() {
      @Override
      public void onTerminated(int exitCode) {
        ProcessIOExecutorService.INSTANCE.execute(() -> {
          ScriptRunnerUtil.terminateProcessHandler(processHandler, 2000, null);
        });
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

  private @NotNull OSProcessHandler createOSProcessHandler(@NotNull KarmaServer server) throws ExecutionException {
    NodeJsInterpreter interpreter = myRunSettings.getInterpreterRef().resolveNotNull(myProject);
    NodeTargetRun targetRun = createTargetRun(interpreter, server);
    OSProcessHandler processHandler = targetRun.startProcessEx().getProcessHandler();
    server.getRestarter().onRunnerExecutionStarted(processHandler);
    ProcessTerminatedListener.attach(processHandler);
    return processHandler;
  }

  private @NotNull NodeTargetRun createTargetRun(@NotNull NodeJsInterpreter interpreter, @NotNull KarmaServer server) throws ExecutionException {
    NodeTargetRun targetRun = new NodeTargetRun(interpreter, myProject, null, NodeTargetRunOptions.of(shouldUsePtyForTestRunners(),
                                                                                                      myRunConfiguration));
    TargetedCommandLineBuilder commandLine = targetRun.getCommandLineBuilder();
    commandLine.setWorkingDirectory(targetRun.path(myRunSettings.getWorkingDirectorySystemDependent()));
    targetRun.addNodeOptionsWithExpandedMacros(false, myRunSettings.getNodeOptions());
    //NodeCommandLineUtil.addNodeOptionsForDebugging(commandLine, Collections.emptyList(), 5858, false, interpreter, true);

    // upload karma-intellij/ folder to the remote if needed
    targetRun.path(KarmaJsSourcesLocator.getInstance().getKarmaIntellijPackageDir());
    Path clientAppFile = KarmaJsSourcesLocator.getInstance().getClientAppFile();
    commandLine.addParameter(targetRun.path(clientAppFile));
    if (NodeJsRemoteInterpreter.isDocker(interpreter) || NodeJsRemoteInterpreter.isDockerCompose(interpreter)) {
      // Workaround for Docker/Docker Compose: assume remove karma server port is forwarded to IDE host with the same port.
      // Need to run karma-runner and karma server in the same Docker container, but it's not possible now.
      Promise<ResolvedPortBinding> resolvedPortBinding = targetRun.localPortBinding(server.getServerPort());
      commandLine.addParameter(TargetValue.create("--serverHost=127.0.0.1", resolvedPortBinding.then((portBinding) -> {
        return "--serverHost=" + portBinding.getTargetEndpoint().getHost();
      })));
      commandLine.addParameter(TargetValue.create("--serverPort=" + server.getServerPort(), resolvedPortBinding.then((portBinding) -> {
        return "--serverPort=" + portBinding.getTargetEndpoint().getPort();
      })));
    }
    else {
      commandLine.addParameter("--serverHost=127.0.0.1");
      commandLine.addParameter("--serverPort=" + server.getServerPort());
    }
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
      myFolder.addPlaceholderText("--testNamePattern=" + testNamesPattern);
    }
    return targetRun;
  }

  private @Nullable String getTestNamesPattern() throws ExecutionException {
    if (myFailedTestNames != null) {
      return JSTestRunnerUtil.getTestsPattern(myFailedTestNames, false);
    }
    if (myRunSettings.getScopeKind() == KarmaScopeKind.TEST_FILE) {
      List<List<JSTestNamePattern>> allFileTests = ReadAction.compute(() -> {
        return findAllFileTests(myProject, myRunSettings.getTestFileSystemIndependentPath());
      });
      String testFileName = PathUtil.getFileName(myRunSettings.getTestFileSystemIndependentPath());
      if (allFileTests.isEmpty()) {
        throw new ExecutionException(KarmaBundle.message("execution.no_tests_found_in_file.dialog.message", testFileName));
      }
      return JSTestRunnerUtil.getTestNamesPattern(allFileTests, false);
    }
    if (myRunSettings.getScopeKind() == KarmaScopeKind.SUITE || myRunSettings.getScopeKind() == KarmaScopeKind.TEST) {
      return JSTestRunnerUtil.buildTestNamesPattern(myProject,
                                                    KarmaDetector.Companion.getInstance(),
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
    List<List<JSTestNamePattern>> allTestsPatterns = KarmaDetector.Companion.getInstance().findAllFileTestPatterns(jsFile);
    if (!allTestsPatterns.isEmpty()) {
      return allTestsPatterns;
    }
    throw new ExecutionException(KarmaBundle.message("execution.no_tests_found_in_file.dialog.message", testFilePath));
  }
}
