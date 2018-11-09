package com.google.jstestdriver.idea.execution;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.jstestdriver.JsTestDriverServer;
import com.google.jstestdriver.idea.common.JstdCommonConstants;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.google.jstestdriver.idea.rt.TestRunner;
import com.google.jstestdriver.idea.rt.util.EscapeUtils;
import com.google.jstestdriver.idea.rt.util.TestFileScope;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.TestProxyFilterProvider;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.javascript.nodejs.NodeCommandLineUtil;
import com.intellij.javascript.testFramework.TestFileStructureManager;
import com.intellij.javascript.testFramework.TestFileStructurePack;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static java.io.File.pathSeparator;

public class JstdRunProfileState implements RunProfileState {

  private static final String JSTD_FRAMEWORK_NAME = "JsTestDriver";
  private static final Function<File, String> GET_ABSOLUTE_PATH = file -> file.getAbsolutePath();

  private final ExecutionEnvironment myEnvironment;
  private final JstdRunSettings myRunSettings;
  private final String myCoverageFilePath;
  private final boolean myDebug;

  public JstdRunProfileState(@NotNull ExecutionEnvironment environment,
                             @NotNull JstdRunSettings runSettings,
                             @Nullable String coverageFilePath) {
    myEnvironment = environment;
    myRunSettings = runSettings;
    myCoverageFilePath = coverageFilePath;
    myDebug = environment.getExecutor().getId().equals(DefaultDebugExecutor.EXECUTOR_ID);
  }

  @NotNull
  public JstdRunSettings getRunSettings() {
    return myRunSettings;
  }

  @Override
  @Nullable
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    if (myRunSettings.isExternalServerType()) {
      return executeWithServer(null);
    }
    JstdServer ideServer = JstdServerRegistry.getInstance().getServer();
    if (ideServer == null || !ideServer.isProcessRunning()) {
      throw new ExecutionException("JsTestDriver server is not running unexpectedly");
    }
    return executeWithServer(ideServer);
  }

  @NotNull
  public ExecutionResult executeWithServer(@Nullable JstdServer ideServer) throws ExecutionException {
    if (!myRunSettings.isExternalServerType() && ideServer == null) {
      throw new ExecutionException("[Internal error] Local JsTestDriver server running in IDE not found");
    }
    ProcessHandler processHandler = createProcessHandler(ideServer);
    ConsoleView consoleView = createSMTRunnerConsoleView(ideServer);
    consoleView.attachToProcess(processHandler);
    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction());
    return executionResult;
  }

  @Nullable
  private String getServerUrl(@Nullable JstdServer ideServer) {
    if (myRunSettings.isExternalServerType()) {
      return myRunSettings.getServerAddress();
    }
    if (ideServer != null && ideServer.isReadyForRunningTests()) {
      return ideServer.getServerUrl();
    }
    return null;
  }

  @NotNull
  private SMTRunnerConsoleView createSMTRunnerConsoleView(@Nullable JstdServer ideServer) {
    JstdRunConfiguration configuration = (JstdRunConfiguration)myEnvironment.getRunProfile();
    JstdTestProxyFilterProvider filterProvider = new JstdTestProxyFilterProvider(myEnvironment.getProject());
    TestConsoleProperties testConsoleProperties = new JstdConsoleProperties(configuration, myEnvironment.getExecutor(), filterProvider);
    String propertyName = SMTestRunnerConnectionUtil.getSplitterPropertyName(JSTD_FRAMEWORK_NAME);
    JstdConsoleView consoleView = new JstdConsoleView(testConsoleProperties, myEnvironment, propertyName, ideServer);
    Disposer.register(myEnvironment.getProject(), consoleView);
    SMTestRunnerConnectionUtil.initConsoleView(consoleView, JSTD_FRAMEWORK_NAME);
    return consoleView;
  }

  @NotNull
  private ProcessHandler createProcessHandler(@Nullable JstdServer ideServer) throws ExecutionException {
    String serverUrl = getServerUrl(ideServer);
    if (serverUrl != null) {
      return createOSProcessHandler(serverUrl);
    }
    final NopProcessHandler nopProcessHandler = new NopProcessHandler();
    if (ideServer != null) {
      ideServer.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
        @Override
        public void onServerTerminated(int exitCode) {
          nopProcessHandler.destroyProcess();
        }
      }, myEnvironment.getProject());
    }
    return nopProcessHandler;
  }

  @NotNull
  private KillableColoredProcessHandler createOSProcessHandler(@NotNull String serverUrl) throws ExecutionException {
    Map<TestRunner.ParameterKey, String> params = createParameterMap(serverUrl);
    GeneralCommandLine commandLine = createCommandLine(params);
    KillableColoredProcessHandler processHandler = NodeCommandLineUtil.createKillableColoredProcessHandler(commandLine, true);
    ProcessTerminatedListener.attach(processHandler);
    return processHandler;
  }

  @NotNull
  private static GeneralCommandLine createCommandLine(@NotNull Map<TestRunner.ParameterKey, String> parameters) {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    // uncomment this if you want to debug jsTestDriver code in the test-runner process
    //commandLine.addParameter("-Xdebug");
    //commandLine.addParameter("-Xrunjdwp:transport=dt_socket,address=5000,server=y,suspend=y");

    File file = new File(PathUtil.getJarPathForClass(JsTestDriverServer.class));
    commandLine.withWorkDirectory(file.getParentFile());

    commandLine.addParameter("-cp");
    commandLine.addParameter(buildClasspath());

    commandLine.addParameter(TestRunner.class.getName());
    for (Map.Entry<TestRunner.ParameterKey, String> param : parameters.entrySet()) {
      String keyValue = EscapeUtils.join(Arrays.asList(param.getKey().name().toLowerCase(Locale.ENGLISH), param.getValue()), '=');
      commandLine.addParameter("--" + keyValue);
    }

    return commandLine;
  }

  private static String buildClasspath() {
    List<File> classpathFiles = getClasspathRootFiles(
      JstdCommonConstants.class,
      TestRunner.class,
      JsTestDriverServer.class,
      Maps.class,
      Gson.class
    );
    Set<String> classpathPaths = ImmutableSet.copyOf(Lists.transform(classpathFiles, GET_ABSOLUTE_PATH));
    return Joiner.on(pathSeparator).join(classpathPaths);
  }

  private static List<File> getClasspathRootFiles(Class<?>... classList) {
    List<File> classpath = Lists.newArrayList();
    for (Class<?> clazz : classList) {
      String path = PathUtil.getJarPathForClass(clazz);
      File file = new File(path);
      classpath.add(file.getAbsoluteFile());
    }
    return classpath;
  }

  @NotNull
  private Map<TestRunner.ParameterKey, String> createParameterMap(@NotNull String serverUrl) throws ExecutionException {
    Map<TestRunner.ParameterKey, String> parameters = Maps.newLinkedHashMap();
    parameters.put(TestRunner.ParameterKey.SERVER_URL, serverUrl);
    TestType testType = myRunSettings.getTestType();
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      parameters.put(TestRunner.ParameterKey.ALL_CONFIGS_IN_DIRECTORY, myRunSettings.getDirectory());
    }
    List<VirtualFile> jstdConfigs = JstdSettingsUtil.collectJstdConfigs(myEnvironment.getProject(), myRunSettings);
    if (jstdConfigs.isEmpty()) {
      throw new ExecutionException("Can't find JsTestDriver configuration file.");
    }
    parameters.put(TestRunner.ParameterKey.CONFIG_FILES, joinJstdConfigs(jstdConfigs));
    TestFileScope testFileScope = buildTestFileScope(myEnvironment.getProject(), myRunSettings);
    if (!testFileScope.isAll()) {
      parameters.put(TestRunner.ParameterKey.TESTS, testFileScope.serialize());
    }

    if (myCoverageFilePath != null) {
      parameters.put(TestRunner.ParameterKey.COVERAGE_OUTPUT_FILE, myCoverageFilePath);
      if (!myRunSettings.getFilesExcludedFromCoverage().isEmpty()) {
        String excludedPaths = EscapeUtils.join(myRunSettings.getFilesExcludedFromCoverage(), ',');
        parameters.put(TestRunner.ParameterKey.COVERAGE_EXCLUDED_PATHS, excludedPaths);
      }
    }
    if (myDebug) {
      parameters.put(TestRunner.ParameterKey.DEBUG, Boolean.TRUE.toString());
    }
    return parameters;
  }

  @NotNull
  private static TestFileScope buildTestFileScope(@NotNull Project project, @NotNull JstdRunSettings settings) throws ExecutionException {
    TestType testType = settings.getTestType();
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY || testType == TestType.CONFIG_FILE) {
      return TestFileScope.allScope();
    }
    if (testType == TestType.JS_FILE) {
      File jsFile = new File(settings.getJsFilePath());
      if (jsFile.isAbsolute() && jsFile.isFile()) {
        VirtualFile virtualFile = VfsUtil.findFileByIoFile(jsFile, true);
        if (virtualFile != null) {
          PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
          if (psiFile instanceof JSFile) {
            JSFile jsPsiFile = (JSFile) psiFile;
            TestFileStructurePack pack = TestFileStructureManager.fetchTestFileStructurePackByJsFile(jsPsiFile);
            if (pack != null) {
              List<String> testCases = pack.getTopLevelElements();
              if (testCases.isEmpty()) {
                throw new ExecutionException("No tests found in " + jsPsiFile.getName());
              }
              Map<String, Set<String>> scope = ContainerUtil.newHashMap();
              for (String testCase : testCases) {
                scope.put(testCase, Collections.emptySet());
              }
              return TestFileScope.customScope(scope);
            }
          }
        }
      }
      throw new ExecutionException("Unable to extract tests from " + jsFile.getName());
    }
    if (testType == TestType.TEST_CASE) {
      Map<String, Set<String>> scope = Collections.singletonMap(settings.getTestCaseName(),
                                                                Collections.emptySet());
      return TestFileScope.customScope(scope);
    }
    if (testType == TestType.TEST_METHOD) {
      Map<String, Set<String>> scope = Collections.singletonMap(settings.getTestCaseName(),
                                                                Collections.singleton(settings.getTestMethodName()));
      return TestFileScope.customScope(scope);
    }
    throw new RuntimeException("Unexpected test type: " + testType);
  }

  @NotNull
  private static String joinJstdConfigs(@NotNull List<VirtualFile> configs) {
    List<String> paths = Lists.newArrayListWithCapacity(configs.size());
    for (VirtualFile config : configs) {
      paths.add(config.getPath());
    }
    return EscapeUtils.join(paths, ',');
  }

  @NotNull
  public static JstdRunProfileState cast(@NotNull RunProfileState state) throws ExecutionException {
    if (state instanceof JstdRunProfileState) {
      return (JstdRunProfileState) state;
    }
    throw new ExecutionException("[Internal error] Cannot run JsTestDriver tests");
  }

  private static class JstdConsoleProperties extends SMTRunnerConsoleProperties {
    private final JstdTestProxyFilterProvider myFilterProvider;

    JstdConsoleProperties(JstdRunConfiguration configuration, Executor executor, JstdTestProxyFilterProvider filterProvider) {
      super(configuration, JSTD_FRAMEWORK_NAME, executor);
      myFilterProvider = filterProvider;
      setUsePredefinedMessageFilter(false);
      setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);
      setIfUndefined(TestConsoleProperties.HIDE_IGNORED_TEST, true);
      setIfUndefined(TestConsoleProperties.SCROLL_TO_SOURCE, true);
      setIdBasedTestTree(true);
      setPrintTestingStartedTime(false);
    }

    @Override
    public SMTestLocator getTestLocator() {
      return JstdTestLocationProvider.INSTANCE;
    }

    @Nullable
    @Override
    public TestProxyFilterProvider getFilterProvider() {
      return myFilterProvider;
    }
  }
}
