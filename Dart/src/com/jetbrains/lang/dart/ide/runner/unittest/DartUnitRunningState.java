package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
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
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ResourceUtil;
import com.intellij.util.text.StringTokenizer;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.DartStackTraceMessageFiler;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartSdkUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author: Fedor.Korotkov
 */
public class DartUnitRunningState extends CommandLineState {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.actions.DartPubAction");

  private static final String DART_FRAMEWORK_NAME = "DartTestRunner";
  private static final String UNIT_CONFIG_FILE_NAME = "jetbrains_unit_config.dart";
  private final DartUnitRunnerParameters myUnitParameters;
  @Nullable
  private final DartSettings myDartSettings;
  private int myDebuggingPort;

  protected DartUnitRunningState(ExecutionEnvironment environment, DartUnitRunnerParameters parameters, DartSettings dartSettings) {
    this(environment, parameters, dartSettings, -1);
  }

  public DartUnitRunningState(ExecutionEnvironment environment,
                              DartUnitRunnerParameters parameters,
                              @Nullable DartSettings dartSettings,
                              int debuggingPort) {
    super(environment);
    myUnitParameters = parameters;
    myDartSettings = dartSettings;
    myDebuggingPort = debuggingPort;
  }

  @Override
  @NotNull
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    ProcessHandler processHandler = startProcess();
    ConsoleView consoleView = createConsole(getEnvironment(), executor);
    consoleView.attachToProcess(processHandler);

    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction());
    return executionResult;
  }

  private ConsoleView createConsole(@NotNull ExecutionEnvironment env,
                                    Executor executor) throws ExecutionException {
    final DartUnitRunConfiguration runConfiguration = (DartUnitRunConfiguration)env.getRunProfile();
    TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(
      new RuntimeConfigurationProducer.DelegatingRuntimeConfiguration<DartUnitRunConfiguration>(runConfiguration),
      DART_FRAMEWORK_NAME,
      executor
    );

    SMTRunnerConsoleView smtConsoleView = SMTestRunnerConnectionUtil.createConsoleWithCustomLocator(
      DART_FRAMEWORK_NAME,
      testConsoleProperties,
      env.getRunnerSettings(),
      env.getConfigurationSettings(),
      new DartTestLocationProvider(),
      true,
      null
    );
    testConsoleProperties.setUsePredefinedMessageFilter(false);
    Filter filter = new DartStackTraceMessageFiler(testConsoleProperties.getProject(), myUnitParameters.getFilePath());
    smtConsoleView.addMessageFilter(filter);

    final Project project = env.getProject();
    assert project != null;
    Disposer.register(project, smtConsoleView);
    return smtConsoleView;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    GeneralCommandLine commandLine = getCommand();

    return new OSProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString());
  }

  public GeneralCommandLine getCommand() throws ExecutionException {
    final GeneralCommandLine commandLine = new GeneralCommandLine();

    final String path = myDartSettings == null ? null : myDartSettings.getSdkPath();
    final String exePath = path == null ? null : DartSdkUtil.getCompilerPathByFolderPath(path);
    if (exePath == null) {
      // todo: fix link
      throw new ExecutionException(DartBundle.message("dart.invalid.sdk"));
    }

    Project project = getEnvironment().getProject();
    assert project != null;
    VirtualFile realFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(myUnitParameters.getFilePath()));
    PsiFile psiFile = realFile != null ? PsiManager.getInstance(project).findFile(realFile) : null;
    if (psiFile != null) {
      String libraryName = DartResolveUtil.getLibraryName(psiFile);
      if (libraryName == null || libraryName.endsWith(".dart")) {
        throw new ExecutionException("Missing library statement in " + psiFile.getName());
      }
    }

    commandLine.setExePath(exePath);
    if (realFile != null) {
      commandLine.setWorkDirectory(realFile.getParent().getPath());
    }
    commandLine.setPassParentEnvironment(true);

    setupUserProperties(commandLine);

    return commandLine;
  }

  private void setupUserProperties(GeneralCommandLine commandLine) throws ExecutionException {
    if (myDartSettings != null) {
      commandLine.getEnvironment().put("com.google.dart.sdk", myDartSettings.getSdkPath());
    }

    commandLine.addParameter("--ignore-unrecognized-flags");

    StringTokenizer argumentsTokenizer = new StringTokenizer(StringUtil.notNullize(myUnitParameters.getVMOptions()));
    while (argumentsTokenizer.hasMoreTokens()) {
      commandLine.addParameter(argumentsTokenizer.nextToken());
    }

    String libUrl = VfsUtilCore.pathToUrl(myUnitParameters.getFilePath());
    final VirtualFile libraryRoot = VirtualFileManager.getInstance().findFileByUrl(libUrl);
    final VirtualFile packages = DartResolveUtil.findPackagesFolder(libraryRoot, getEnvironment().getProject());
    if (packages != null && packages.isDirectory()) {
      commandLine.addParameter("--package-root=" + packages.getPath() + "/");
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
      file.createNewFile();
    }

    final DartUnitRunnerParameters.Scope scope = myUnitParameters.getScope();
    final String name = myUnitParameters.getTestName();

    String runnerCode = getRunnerCode();
    runnerCode = runnerCode.replaceFirst("DART_UNITTEST", getUnitPath(myUnitParameters.getFilePath()));
    runnerCode = runnerCode.replaceFirst("NAME", StringUtil.notNullize(name));
    runnerCode = runnerCode.replaceFirst("SCOPE", scope.toString());
    runnerCode = runnerCode.replaceFirst("TEST_FILE_PATH", pathToDartUrl(myUnitParameters.getFilePath()));

    FileUtil.writeToFile(file, runnerCode);

    return file.getAbsolutePath();
  }

  private String getUnitPath(String path) {
    VirtualFile libRoot = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(path));
    VirtualFile packagesFolder = DartResolveUtil.findPackagesFolder(libRoot, getEnvironment().getProject());
    if (myDartSettings == null || (packagesFolder != null && packagesFolder.findChild("unittest") != null)) {
      return "package:unittest/unittest.dart";
    }
    return pathToDartUrl(StringUtil.notNullize(myDartSettings.getSdkPath()) + "/pkg/unittest/unittest.dart");
  }

  private static String pathToDartUrl(@NonNls @NotNull String path) {
    return VfsUtilCore.pathToUrl(path);
  }

  private static String getRunnerCode() throws IOException {
    final URL resource = ResourceUtil.getResource(DartUnitRunningState.class, "/config", UNIT_CONFIG_FILE_NAME);
    return ResourceUtil.loadText(resource);
  }
}
