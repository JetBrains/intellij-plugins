package com.google.jstestdriver.idea.execution;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.internal.ImmutableSet;
import com.google.jstestdriver.JsTestDriverServer;
import com.google.jstestdriver.idea.TestRunner;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowPanel;
import com.google.jstestdriver.idea.util.EscapeUtils;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.TestProxyPrinterProvider;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.io.File.pathSeparator;

/**
 * @author Sergey Simonchik
 */
public class JstdTestRunnerCommandLineState extends CommandLineState {

  private static final String JSTD_FRAMEWORK_NAME = "JsTestDriver";
  private static final Function<File, String> GET_ABSOLUTE_PATH = new Function<File, String>() {
    @Override
    public String apply(File file) {
      return file.getAbsolutePath();
    }
  };

  private final Project myProject;
  private final ExecutionEnvironment myExecutionEnvironment;
  private final JstdRunSettings myRunSettings;
  private final String myCoverageFilePath;

  public JstdTestRunnerCommandLineState(
    @NotNull Project project,
    @NotNull ExecutionEnvironment executionEnvironment,
    @NotNull JstdRunSettings runSettings,
    @Nullable String coverageFilePath)
  {
    super(executionEnvironment);
    myProject = project;
    myExecutionEnvironment = executionEnvironment;
    myRunSettings = runSettings;
    myCoverageFilePath = coverageFilePath;
  }

  @Override
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    ProcessHandler processHandler = startProcess();
    ConsoleView consoleView = createConsole(myProject, myExecutionEnvironment, executor);
    consoleView.attachToProcess(processHandler);

    //consoleView.addMessageFilter(new NodeJSStacktraceFilter(myProject));

    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction());
    return executionResult;
  }

  private static ConsoleView createConsole(@NotNull Project project,
                                           @NotNull ExecutionEnvironment env,
                                           Executor executor)
    throws ExecutionException {
    JstdRunConfiguration runConfiguration = (JstdRunConfiguration) env.getRunProfile();
    TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(
      new RuntimeConfigurationProducer.DelegatingRuntimeConfiguration<JstdRunConfiguration>(runConfiguration),
      JSTD_FRAMEWORK_NAME,
      executor
    );

    JstdTestProxyPrinterProvider printerProvider = new JstdTestProxyPrinterProvider();
    SMTRunnerConsoleView smtConsoleView = SMTestRunnerConnectionUtil.createConsoleWithCustomLocator(
      JSTD_FRAMEWORK_NAME,
      testConsoleProperties,
      env.getRunnerSettings(),
      env.getConfigurationSettings(),
      new JstdTestLocationProvider(),
      true,
      printerProvider
    );
    printerProvider.setConsoleView(smtConsoleView);

    Disposer.register(project, smtConsoleView);
    return smtConsoleView;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    Map<TestRunner.ParameterKey, String> params = createParameterMap();
    GeneralCommandLine commandLine = createCommandLine(params);

    OSProcessHandler osProcessHandler = new OSProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString());
    ProcessTerminatedListener.attach(osProcessHandler);
    return osProcessHandler;
  }

  @NotNull
  private static GeneralCommandLine createCommandLine(@NotNull Map<TestRunner.ParameterKey, String> parameters) {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    // uncomment this if you want to debug jsTestDriver code in the test-runner process
    //addParameter("-Xdebug");
    //addParameter("-Xrunjdwp:transport=dt_socket,address=5000,server=y,suspend=y");

    File file = new File(PathUtil.getJarPathForClass(JsTestDriverServer.class));
    commandLine.setWorkDirectory(file.getParentFile());

    commandLine.addParameter("-cp");
    commandLine.addParameter(buildClasspath());

    commandLine.addParameter(TestRunner.class.getName());
    for (Map.Entry<TestRunner.ParameterKey, String> param : parameters.entrySet()) {
      String keyValue = EscapeUtils.join(Arrays.asList(param.getKey().name().toLowerCase(), param.getValue()), '=');
      commandLine.addParameter("--" + keyValue);
    }

    return commandLine;
  }

  private static String buildClasspath() {
    List<File> classpathFiles = getClasspathRootFiles(TestRunner.class, JsTestDriverServer.class);
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

  public Map<TestRunner.ParameterKey, String> createParameterMap() {
    Map<TestRunner.ParameterKey, String> parameters = Maps.newLinkedHashMap();
    String serverUrl = myRunSettings.getServerType() == ServerType.INTERNAL ?
                       "http://localhost:" + JstdToolWindowPanel.serverPort :
                       myRunSettings.getServerAddress();
    parameters.put(TestRunner.ParameterKey.SERVER_URL, serverUrl);
    if (myRunSettings.getTestType() == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      parameters.put(TestRunner.ParameterKey.ALL_CONFIGS_IN_DIRECTORY, myRunSettings.getDirectory());
    }
    List<VirtualFile> jstdConfigs = JstdSettingsUtil.collectJstdConfigs(myProject, myRunSettings);
    parameters.put(TestRunner.ParameterKey.CONFIG_FILES, joinJstdConfigs(jstdConfigs));
    if (myRunSettings.getTestType() == TestType.TEST_CASE) {
      parameters.put(TestRunner.ParameterKey.TEST_CASE, myRunSettings.getTestCaseName());
    }
    if (myRunSettings.getTestType() == TestType.TEST_METHOD) {
      parameters.put(TestRunner.ParameterKey.TEST_CASE, myRunSettings.getTestCaseName());
      parameters.put(TestRunner.ParameterKey.TEST_METHOD, myRunSettings.getTestMethodName());
    }
    if (myCoverageFilePath != null) {
      parameters.put(TestRunner.ParameterKey.COVERAGE_OUTPUT_FILE, myCoverageFilePath);
      if (!myRunSettings.getFilesExcludedFromCoverage().isEmpty()) {
        String excludedPaths = EscapeUtils.join(myRunSettings.getFilesExcludedFromCoverage(), ',');
        parameters.put(TestRunner.ParameterKey.COVERAGE_EXCLUDED_PATHS, excludedPaths);
      }
    }
    return parameters;
  }

  @NotNull
  private static String joinJstdConfigs(@NotNull List<VirtualFile> configs) {
    List<String> paths = Lists.newArrayListWithCapacity(configs.size());
    for (VirtualFile config : configs) {
      paths.add(config.getPath());
    }
    return EscapeUtils.join(paths, ',');
  }

  private static class JstdTestProxyPrinterProvider implements TestProxyPrinterProvider {
    private BaseTestsOutputConsoleView myConsoleView;

    @Override
    public Printer getPrinterByType(@NotNull String nodeType, @Nullable String arguments) {
      BaseTestsOutputConsoleView consoleView = myConsoleView;
      if (consoleView == null) {
        return null;
      }
      if ("browser".equals(nodeType) && arguments != null) {
        List<String> args = EscapeUtils.split(arguments, ',');
        if (args.size() == 2) {
          File basePath = new File(args.get(0));
          String browserName = args.get(1);
          if (basePath.isAbsolute() && basePath.isDirectory()) {
            return new StacktracePrinter(consoleView, basePath, browserName);
          }
        }
      }
      return null;
    }

    public void setConsoleView(@NotNull BaseTestsOutputConsoleView consoleView) {
      myConsoleView = consoleView;
    }
  }
}
