package com.jetbrains.lang.dart.ide.runner.test;

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
import com.intellij.execution.testframework.TestFrameworkRunningModel;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunningState;
import com.jetbrains.lang.dart.ide.runner.util.DartTestLocationProvider;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartTestRunningState extends DartCommandLineRunningState {
  public static final String DART_FRAMEWORK_NAME = "DartTestRunner";
  private static final String PUB_SNAPSHOT_PATH = "/bin/snapshots/pub.dart.snapshot";
  private static final String RUN_COMMAND = "run";
  private static final String TEST_PACKAGE_SPEC = "test:test";
  private static final String EXPANDED_REPORTER_OPTION = "-r json";

  public DartTestRunningState(final @NotNull ExecutionEnvironment environment) throws ExecutionException {
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

    if (ActionManager.getInstance().getAction("RerunFailedTests") != null) {
      DartConsoleProperties properties = (DartConsoleProperties)((SMTRunnerConsoleView)consoleView).getProperties();
      AbstractRerunFailedTestsAction rerunFailedTestsAction = properties.createRerunFailedTestsAction(consoleView);
      assert rerunFailedTestsAction != null;
      rerunFailedTestsAction.setModelProvider(() -> ((SMTRunnerConsoleView)consoleView).getResultsViewer());
      executionResult.setRestartActions(rerunFailedTestsAction, new ToggleAutoTestAction());
    }
    else {
      executionResult.setRestartActions(new ToggleAutoTestAction());
    }

    return executionResult;
  }

  private static ConsoleView createConsole(@NotNull ExecutionEnvironment env) {
    final Project project = env.getProject();
    final DartRunConfiguration runConfiguration = (DartRunConfiguration)env.getRunProfile();
    final DartTestRunnerParameters runnerParameters = (DartTestRunnerParameters)runConfiguration.getRunnerParameters();

    final TestConsoleProperties testConsoleProperties = new DartConsoleProperties(runConfiguration, env);
    final ConsoleView consoleView = SMTestRunnerConnectionUtil.createConsole(DART_FRAMEWORK_NAME, testConsoleProperties);

    try {
      final VirtualFile dartFile = runnerParameters.getDartFileOrDirectory();
      consoleView.addMessageFilter(new DartConsoleFilter(project, dartFile));
      consoleView.addMessageFilter(new DartRelativePathsConsoleFilter(project, runnerParameters.computeProcessWorkingDirectory(project)));
      consoleView.addMessageFilter(new UrlFilter());
    }
    catch (RuntimeConfigurationError ignore) {/* can't happen because already checked */}

    Disposer.register(project, consoleView);
    return consoleView;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    Project project = getEnvironment().getProject();

    DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) throw new ExecutionException("Dart SDK cannot be found"); // can't happen, already checked

    DartTestRunnerParameters params = getParameters();

    StringBuilder builder = new StringBuilder();
    builder.append(RUN_COMMAND);

    final boolean projectWithoutPubspec = Registry.is("dart.projects.without.pubspec", false);
    final String targetName = params.getTargetName();
    final String testRunnerOptions = params.getTestRunnerOptions();

    if (projectWithoutPubspec &&
        params.getScope() == DartTestRunnerParameters.Scope.FOLDER &&
        targetName != null &&
        !targetName.isEmpty()) {
      builder.append(" ").append(":").append(targetName).append(" ").append(EXPANDED_REPORTER_OPTION);
      if (testRunnerOptions != null && !testRunnerOptions.isEmpty()) {
        builder.append(" ").append(testRunnerOptions);
      }
    }
    else {
      builder.append(' ').append(TEST_PACKAGE_SPEC);
      builder.append(' ').append(EXPANDED_REPORTER_OPTION);
      if (testRunnerOptions != null && !testRunnerOptions.isEmpty()) {
        builder.append(" ").append(testRunnerOptions);
      }
      builder.append(' ').append(params.getFilePath());

      if (params.getScope() == DartTestRunnerParameters.Scope.GROUP_OR_TEST_BY_NAME) {
        builder.append(" -N \"").append(StringUtil.notNullize(params.getTestName())).append("\"");
      }

      if (params.getScope() == DartTestRunnerParameters.Scope.MULTIPLE_NAMES) {
        final String regex = StringUtil.notNullize(params.getTestName());
        // may be empty only in case of Rerun Failed Tests when there are no failed ones yet
        if (regex.isEmpty()) {
          throw new ExecutionException("No tests to run");
        }
        builder.append(" -n \"").append(regex).append("\"");
      }
    }

    params.setArguments(builder.toString());
    // working directory is not configurable in UI because there's only one valid value that we calculate ourselves
    params.setWorkingDirectory(params.computeProcessWorkingDirectory(project));
    return doStartProcess(pathToDartUrl(sdk.getHomePath() + PUB_SNAPSHOT_PATH));
  }

  DartTestRunnerParameters getParameters() {
    return (DartTestRunnerParameters)myRunnerParameters;
  }

  private static String pathToDartUrl(@NonNls @NotNull String path) {
    final String url = VfsUtilCore.pathToUrl(path);
    return SystemInfo.isWindows ? url.replace("file://", "file:///") : url;
  }

  private static class DartConsoleProperties extends SMTRunnerConsoleProperties implements SMCustomMessagesParsing {
    public DartConsoleProperties(DartRunConfiguration runConfiguration, ExecutionEnvironment env) {
      super(runConfiguration, DART_FRAMEWORK_NAME, env.getExecutor());
      setUsePredefinedMessageFilter(false);
      setIdBasedTestTree(true);
    }

    @Nullable
    @Override
    public SMTestLocator getTestLocator() {
      return DartTestLocationProvider.INSTANCE;
    }

    @Override
    public OutputToGeneralTestEventsConverter createTestEventsConverter(@NotNull String testFrameworkName,
                                                                        @NotNull TestConsoleProperties consoleProperties) {
      return new DartTestEventsConverter(testFrameworkName, consoleProperties);
    }

    @Nullable
    @Override
    public AbstractRerunFailedTestsAction createRerunFailedTestsAction(ConsoleView consoleView) {
      if (ActionManager.getInstance().getAction("RerunFailedTests") == null) return null; // backward compatibility

      DartTestRerunnerAction action = new DartTestRerunnerAction(consoleView);
      action.init(this);
      return action;
    }
  }
}
