package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.filters.UrlFilter;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.net.NetUtils;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.ide.runner.test.DartTestRunnerParameters;
import com.jetbrains.lang.dart.pubServer.PubServerManager;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringTokenizer;

public class DartCommandLineRunningState extends CommandLineState {

  protected final @NotNull DartCommandLineRunnerParameters myRunnerParameters;
  private int myDebuggingPort = -1;
  private int myObservatoryPort = -1;

  public DartCommandLineRunningState(final @NotNull ExecutionEnvironment env) throws ExecutionException {
    super(env);
    myRunnerParameters = ((DartRunConfigurationBase)env.getRunProfile()).getRunnerParameters().clone();

    try {
      myRunnerParameters.check(env.getProject());
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e);
    }

    final TextConsoleBuilder builder = getConsoleBuilder();
    if (builder instanceof TextConsoleBuilderImpl) {
      ((TextConsoleBuilderImpl)builder).setUsePredefinedMessageFilter(false);
    }

    try {
      builder.addFilter(new DartConsoleFilter(env.getProject(), myRunnerParameters.getDartFileOrDirectory()));

      // unit tests can be run as normal Dart apps, so add DartUnitConsoleFilter as well
      final String workingDir = myRunnerParameters.computeProcessWorkingDirectory();
      builder.addFilter(new DartRelativePathsConsoleFilter(env.getProject(), workingDir));
      builder.addFilter(new UrlFilter());
    }
    catch (RuntimeConfigurationError e) {
      builder.addFilter(new DartConsoleFilter(env.getProject(), null)); // can't happen because already checked
    }
  }

  @Override
  protected AnAction[] createActions(final ConsoleView console, final ProcessHandler processHandler, final Executor executor) {
    // These action is effectively added only to the Run tool window. For Debug see DartCommandLineDebugProcess.registerAdditionalActions()
    final AnAction[] actions = super.createActions(console, processHandler, executor);
    final AnAction[] newActions = new AnAction[actions.length + 2];
    System.arraycopy(actions, 0, newActions, 0, actions.length);

    newActions[newActions.length - 2] = new Separator();

    newActions[newActions.length - 1] = new OpenDartObservatoryUrlAction(myObservatoryPort, new Computable<Boolean>() {
      @Override
      public Boolean compute() {
        return !processHandler.isProcessTerminated();
      }
    });

    return newActions;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    return doStartProcess(null);
  }

  protected ProcessHandler doStartProcess(final @Nullable String overriddenMainFilePath) throws ExecutionException {
    final GeneralCommandLine commandLine = createCommandLine(overriddenMainFilePath);
    final OSProcessHandler processHandler = new ColoredProcessHandler(commandLine);

    // Commented out code is a workaround for "Observatory listening on ..." message that is concatenated (without line break) with the message following it
    // The problem is not actula at the moment because Observatory is not turned on for tests
    //final OSProcessHandler processHandler = new ColoredProcessHandler(commandLine) {
    //  @Override
    //  public void coloredTextAvailable(String text, Key attributes) {
    //    if (text.startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON)) {
    //      text += "\n";
    //    }
    //    super.coloredTextAvailable(text, attributes);
    //  }
    //};

    ProcessTerminatedListener.attach(processHandler, getEnvironment().getProject());
    return processHandler;
  }

  private GeneralCommandLine createCommandLine(final @Nullable String overriddenMainFilePath) throws ExecutionException {
    final DartSdk sdk = DartSdk.getDartSdk(getEnvironment().getProject());
    if (sdk == null) {
      throw new ExecutionException(DartBundle.message("dart.sdk.is.not.configured"));
    }

    final String dartExePath = DartSdkUtil.getDartExePath(sdk);

    final String workingDir;
    try {
      workingDir = myRunnerParameters.computeProcessWorkingDirectory();
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e); // can't happen because already checked
    }

    final GeneralCommandLine commandLine = new GeneralCommandLine().withWorkDirectory(workingDir);
    commandLine.setCharset(CharsetToolkit.UTF8_CHARSET);
    commandLine.setExePath(FileUtil.toSystemDependentName(dartExePath));
    commandLine.getEnvironment().putAll(myRunnerParameters.getEnvs());
    commandLine
      .withParentEnvironmentType(myRunnerParameters.isIncludeParentEnvs() ? ParentEnvironmentType.CONSOLE : ParentEnvironmentType.NONE);
    setupParameters(getEnvironment().getProject(), sdk, commandLine, myRunnerParameters, overriddenMainFilePath);

    return commandLine;
  }

  private void setupParameters(@NotNull final Project project,
                               @NotNull final DartSdk sdk,
                               @NotNull final GeneralCommandLine commandLine,
                               @NotNull final DartCommandLineRunnerParameters runnerParameters,
                               @Nullable final String overriddenMainFilePath) throws ExecutionException {
    // TODO Clean up dialog box and trim unused VM options here.
    commandLine.addParameter("--ignore-unrecognized-flags");

    int customObservatoryPort = -1;

    final String vmOptions = runnerParameters.getVMOptions();
    if (vmOptions != null) {
      final StringTokenizer vmOptionsTokenizer = new CommandLineTokenizer(vmOptions);
      while (vmOptionsTokenizer.hasMoreTokens()) {
        final String vmOption = vmOptionsTokenizer.nextToken();
        commandLine.addParameter(vmOption);

        try {
          if (vmOption.startsWith("--enable-vm-service:")) {
            customObservatoryPort = Integer.parseInt(vmOption.substring("--enable-vm-service:".length()));
          }
          if (vmOption.startsWith("--observe:")) {
            customObservatoryPort = Integer.parseInt(vmOption.substring("--observe:".length()));
          }
        }
        catch (NumberFormatException ignore) {/**/}
      }
    }

    if (runnerParameters.isCheckedMode()) {
      commandLine.addParameter(DartiumUtil.CHECKED_MODE_OPTION);
    }

    final VirtualFile dartFile;
    try {
      dartFile = runnerParameters.getDartFileOrDirectory();
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e);
    }

    final VirtualFile packageRoot = DartUrlResolver.getInstance(project, dartFile).getPackageRoot();
    if (packageRoot != null) {
      // more than one package root is not supported by the [SDK]/bin/dart tool
      commandLine.addParameter("--package-root=" + FileUtil.toSystemDependentName(packageRoot.getPath()));
    }

    if (DefaultDebugExecutor.EXECUTOR_ID.equals(getEnvironment().getExecutor().getId())) {
      if (StringUtil.compareVersionNumbers(sdk.getVersion(), "1.14") < 0) {
        myDebuggingPort = NetUtils.tryToFindAvailableSocketPort();
        commandLine.addParameter("--debug:" + myDebuggingPort);
        commandLine.addParameter("--break-at-isolate-spawn");
      }
      else {
        commandLine.addParameter("--pause_isolates_on_start");
      }
    }

    if (customObservatoryPort > 0) {
      myObservatoryPort = customObservatoryPort;
    }
    else if (!(myRunnerParameters instanceof DartTestRunnerParameters)) {
      myObservatoryPort = PubServerManager.findOneMoreAvailablePort(myDebuggingPort);
      commandLine.addParameter("--enable-vm-service:" + myObservatoryPort);
    }

    commandLine.addParameter("--trace_service_pause_events");

    commandLine.addParameter(FileUtil.toSystemDependentName(overriddenMainFilePath == null ? dartFile.getPath() : overriddenMainFilePath));

    final String arguments = runnerParameters.getArguments();
    if (arguments != null) {
      StringTokenizer argumentsTokenizer = new CommandLineTokenizer(arguments);
      while (argumentsTokenizer.hasMoreTokens()) {
        commandLine.addParameter(argumentsTokenizer.nextToken());
      }
    }
  }

  public int getDebuggingPort() {
    return myDebuggingPort;
  }

  public int getObservatoryPort() {
    return myObservatoryPort;
  }
}
