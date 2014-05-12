package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.ResourceUtil;
import com.intellij.util.text.StringTokenizer;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DartUnitRunningState extends CommandLineState {
  private static final Logger LOG = Logger.getInstance("com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunningState");

  private static final String DART_FRAMEWORK_NAME = "DartTestRunner";
  private static final String UNIT_CONFIG_FILE_NAME = "jetbrains_unit_config.dart";
  private final DartUnitRunnerParameters myUnitParameters;
  private final DartSdk myDartSdk;
  private int myDebuggingPort;

  protected DartUnitRunningState(ExecutionEnvironment environment, DartUnitRunnerParameters parameters, DartSdk sdk) {
    this(environment, parameters, sdk, -1);
  }

  public DartUnitRunningState(ExecutionEnvironment environment,
                              DartUnitRunnerParameters parameters,
                              DartSdk sdk,
                              int debuggingPort) {
    super(environment);
    myUnitParameters = parameters;
    myDartSdk = sdk;
    myDebuggingPort = debuggingPort;
  }

  @Override
  @NotNull
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    ProcessHandler processHandler = startProcess();
    ConsoleView consoleView = createConsole(getEnvironment());
    consoleView.attachToProcess(processHandler);

    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction(getEnvironment()));
    return executionResult;
  }

  private static ConsoleView createConsole(@NotNull ExecutionEnvironment env) throws ExecutionException {
    final Project project = env.getProject();
    final DartUnitRunConfiguration runConfiguration = (DartUnitRunConfiguration)env.getRunProfile();
    final DartUnitRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();

    VirtualFile file = null;
    final String filePath = runnerParameters.getFilePath();
    if (filePath != null) {
      file = LocalFileSystem.getInstance().findFileByPath(filePath);
    }

    final DartConsoleFilter filter = new DartConsoleFilter(project, file);

    TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(runConfiguration, DART_FRAMEWORK_NAME, env.getExecutor());
    testConsoleProperties.setUsePredefinedMessageFilter(false);

    SMTRunnerConsoleView smtConsoleView = SMTestRunnerConnectionUtil.createConsoleWithCustomLocator(
      DART_FRAMEWORK_NAME,
      testConsoleProperties,
      env,
      new DartTestLocationProvider(),
      true,
      null
    );

    smtConsoleView.addMessageFilter(filter);

    Disposer.register(project, smtConsoleView);
    return smtConsoleView;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    GeneralCommandLine commandLine = getCommand();

    final OSProcessHandler processHandler = new OSProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString());
    ProcessTerminatedListener.attach(processHandler, getEnvironment().getProject());
    return processHandler;
  }

  public GeneralCommandLine getCommand() throws ExecutionException {
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    final String filePath = myUnitParameters.getFilePath();
    final VirtualFile realFile = filePath == null ? null : VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(filePath));

    commandLine.setExePath(DartSdkUtil.getDartExePath(myDartSdk));
    final String workingDirectory = myUnitParameters.getWorkingDirectory();
    if (workingDirectory != null) {
      commandLine.setWorkDirectory(workingDirectory);
    } else if (realFile != null) {
      commandLine.setWorkDirectory(realFile.getParent().getPath());
    }
    commandLine.setPassParentEnvironment(true);

    setupUserProperties(commandLine);

    return commandLine;
  }

  private void setupUserProperties(GeneralCommandLine commandLine) throws ExecutionException {
    commandLine.addParameter("--ignore-unrecognized-flags");

    StringTokenizer argumentsTokenizer = new StringTokenizer(StringUtil.notNullize(myUnitParameters.getVMOptions()));
    while (argumentsTokenizer.hasMoreTokens()) {
      commandLine.addParameter(argumentsTokenizer.nextToken());
    }

    final String filePath = myUnitParameters.getFilePath();
    if (filePath != null) {
      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
      if (file != null) {
        final VirtualFile[] packageRoots = DartUrlResolver.getInstance(getEnvironment().getProject(), file).getPackageRoots();
        if (packageRoots.length > 0) {
          // more than one package root is not supported by the [SDK]/bin/dart tool
          commandLine.addParameter("--package-root=" + packageRoots[0].getPath());
        }
      }
    }

    if (myDebuggingPort > 0) {
      commandLine.addParameter("--debug:" + myDebuggingPort);
    }

    try {
      commandLine.addParameter(createPatchedFile());
    }
    catch (IOException e) {
      LOG.debug(e);
      throw new ExecutionException("Can't create runner!");
    }

    argumentsTokenizer = new StringTokenizer(StringUtil.notNullize(myUnitParameters.getArguments()));
    while (argumentsTokenizer.hasMoreTokens()) {
      commandLine.addParameter(argumentsTokenizer.nextToken());
    }
  }

  private String createPatchedFile() throws IOException {
    final File file = new File(FileUtil.getTempDirectory(), UNIT_CONFIG_FILE_NAME);
    if (!file.exists()) {
      //noinspection ResultOfMethodCallIgnored
      file.createNewFile();
    }

    final DartUnitRunnerParameters.Scope scope = myUnitParameters.getScope();
    final String name = myUnitParameters.getTestName();

    String runnerCode = getRunnerCode();
    runnerCode = runnerCode.replaceFirst("DART_UNITTEST", "package:unittest/unittest.dart");
    runnerCode = runnerCode.replaceFirst("NAME", StringUtil.notNullize(name));
    runnerCode = runnerCode.replaceFirst("SCOPE", scope == null ? "" : scope.toString());
    final String filePath = myUnitParameters.getFilePath();
    runnerCode = runnerCode.replaceFirst("TEST_FILE_PATH", filePath == null ? "" : pathToDartUrl(filePath));

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
}
