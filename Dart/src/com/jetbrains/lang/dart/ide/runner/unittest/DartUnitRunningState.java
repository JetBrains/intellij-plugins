package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.filters.UrlFilter;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ResourceUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunningState;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DartUnitRunningState extends DartCommandLineRunningState {
  private static final String DART_FRAMEWORK_NAME = "DartTestRunner";
  private static final String UNIT_CONFIG_FILE_NAME = "jetbrains_unit_config.dart";

  public DartUnitRunningState(final @NotNull ExecutionEnvironment environment) throws ExecutionException {
    super(environment);
  }

  @Override
  @NotNull
  public ExecutionResult execute(final @NotNull Executor executor, final @NotNull ProgramRunner runner) throws ExecutionException {
    final ProcessHandler processHandler = startProcess();
    final ConsoleView consoleView = createConsole(getEnvironment());
    consoleView.attachToProcess(processHandler);

    final DefaultExecutionResult executionResult =
      new DefaultExecutionResult(consoleView, processHandler, createActions(consoleView, processHandler, executor));
    executionResult.setRestartActions(new ToggleAutoTestAction());
    return executionResult;
  }

  private static ConsoleView createConsole(@NotNull ExecutionEnvironment env) {
    final Project project = env.getProject();
    final DartUnitRunConfiguration runConfiguration = (DartUnitRunConfiguration)env.getRunProfile();
    final DartUnitRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();

    final TestConsoleProperties testConsoleProperties = new DartConsoleProperties(runConfiguration, env);
    final ConsoleView consoleView = SMTestRunnerConnectionUtil.createConsole(DART_FRAMEWORK_NAME, testConsoleProperties, env);

    try {
      final VirtualFile dartFile = runnerParameters.getDartFile();
      consoleView.addMessageFilter(new DartConsoleFilter(project, dartFile));

      final String workingDir = StringUtil.isEmptyOrSpaces(runnerParameters.getWorkingDirectory())
                                ? dartFile.getParent().getPath()
                                : runnerParameters.getWorkingDirectory();
      consoleView.addMessageFilter(new DartRelativePathsConsoleFilter(project, workingDir));
      consoleView.addMessageFilter(new UrlFilter());
    }
    catch (RuntimeConfigurationError ignore) {/**/}

    Disposer.register(project, consoleView);
    return consoleView;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    final String testRunnerPath;
    try {
      testRunnerPath = createTestRunnerFile();
    }
    catch (IOException e) {
      throw new ExecutionException(DartBundle.message("failed.to.create.test.runner", e.getMessage()));
    }

    return doStartProcess(testRunnerPath);
  }

  private String createTestRunnerFile() throws IOException {
    final File file = new File(FileUtil.getTempDirectory(), UNIT_CONFIG_FILE_NAME);
    if (!file.exists()) {
      //noinspection ResultOfMethodCallIgnored
      file.createNewFile();
    }

    final DartUnitRunnerParameters.Scope scope = ((DartUnitRunnerParameters)myRunnerParameters).getScope();
    final String name = ((DartUnitRunnerParameters)myRunnerParameters).getTestName();

    String runnerCode = getRunnerCode();
    runnerCode = runnerCode.replaceFirst("DART_UNITTEST", "package:unittest/unittest.dart");
    runnerCode = runnerCode.replaceFirst("NAME", StringUtil.notNullize(name));
    runnerCode = runnerCode.replaceFirst("SCOPE", scope.toString());
    final String filePath = myRunnerParameters.getFilePath();
    runnerCode = runnerCode.replaceFirst("TEST_FILE_URI", filePath == null ? "" : pathToDartUrl(filePath));
    runnerCode = runnerCode.replaceFirst("TEST_FILE_PATH", filePath == null ? "" : filePath);

    FileUtil.writeToFile(file, runnerCode);

    return file.getAbsolutePath();
  }

  private static String pathToDartUrl(@NonNls @NotNull String path) {
    final String url = VfsUtilCore.pathToUrl(path);
    return SystemInfo.isWindows ? url.replace("file://", "file:///") : url;
  }

  private static String getRunnerCode() throws IOException {
    final URL resource = ResourceUtil.getResource(DartUnitRunningState.class, "/config", UNIT_CONFIG_FILE_NAME);
    return ResourceUtil.loadText(resource);
  }

  private static class DartConsoleProperties extends SMTRunnerConsoleProperties {
    public DartConsoleProperties(DartUnitRunConfiguration runConfiguration, ExecutionEnvironment env) {
      super(runConfiguration, DART_FRAMEWORK_NAME, env.getExecutor());
      setUsePredefinedMessageFilter(false);
      setIdBasedTestTree(true);
    }

    @Nullable
    @Override
    public SMTestLocator getTestLocator() {
      return DartTestLocationProvider.INSTANCE;
    }
  }
}
