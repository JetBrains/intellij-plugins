package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;

import java.util.StringTokenizer;

public class DartCommandLineRunningState extends CommandLineState {
  private final @NotNull DartCommandLineRunnerParameters myRunnerParameters;
  private final int myDebuggingPort;

  public DartCommandLineRunningState(final @NotNull ExecutionEnvironment env,
                                     final @NotNull DartCommandLineRunnerParameters runnerParameters,
                                     final int debuggingPort) {
    super(env);
    myRunnerParameters = runnerParameters;
    myDebuggingPort = debuggingPort;

    final TextConsoleBuilder builder = getConsoleBuilder();
    if (builder instanceof TextConsoleBuilderImpl) {
      ((TextConsoleBuilderImpl)builder).setUsePredefinedMessageFilter(false);
    }

    final String path = myRunnerParameters.getFilePath();
    final VirtualFile contextFile = StringUtil.isEmptyOrSpaces(path) ? null : LocalFileSystem.getInstance().findFileByPath(path);
    builder.addFilter(new DartConsoleFilter(env.getProject(), contextFile));
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    final OSProcessHandler processHandler = new OSProcessHandler(createCommandLine());
    ProcessTerminatedListener.attach(processHandler, getEnvironment().getProject());
    return processHandler;
  }

  private GeneralCommandLine createCommandLine() throws ExecutionException {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      throw new ExecutionException(DartBundle.message("dart.sdk.is.not.configured"));
    }

    final String dartExePath = DartSdkUtil.getDartExePath(sdk);

    final String filePath = myRunnerParameters.getFilePath();
    if (StringUtil.isEmptyOrSpaces(filePath)) {
      throw new ExecutionException(DartBundle.message("path.to.dart.file.not.set"));
    }

    final VirtualFile mainDartFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (mainDartFile == null) {
      throw new ExecutionException(DartBundle.message("dart.file.not.found", filePath));
    }

    final String workingDir = StringUtil.notNullize(myRunnerParameters.getWorkingDirectory(), PathUtil.getParentPath(filePath));

    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(dartExePath);
    commandLine.setWorkDirectory(workingDir);
    commandLine.getEnvironment().putAll(myRunnerParameters.getEnvs());
    commandLine.setPassParentEnvironment(myRunnerParameters.isIncludeParentEnvs());
    setupParameters(commandLine, mainDartFile);

    return commandLine;
  }

  private void setupParameters(final @NotNull GeneralCommandLine commandLine, final @NotNull VirtualFile mainDartFile) {
    commandLine.addParameter("--ignore-unrecognized-flags");

    final String vmOptions = myRunnerParameters.getVMOptions();
    if (vmOptions != null) {
      final StringTokenizer vmOptionsTokenizer = new CommandLineTokenizer(vmOptions);
      while (vmOptionsTokenizer.hasMoreTokens()) {
        commandLine.addParameter(vmOptionsTokenizer.nextToken());
      }
    }

    final VirtualFile[] packageRoots = DartUrlResolver.getInstance(getEnvironment().getProject(), mainDartFile).getPackageRoots();
    if (packageRoots.length > 0) {
      // more than one package root is not supported by the [SDK]/bin/dart tool
      commandLine.addParameter("--package-root=" + packageRoots[0].getPath());
    }

    if (myDebuggingPort > 0) {
      commandLine.addParameter("--debug:" + myDebuggingPort);
    }

    commandLine.addParameter(mainDartFile.getPath());

    final String arguments = myRunnerParameters.getArguments();
    if (arguments != null) {
      StringTokenizer argumentsTokenizer = new CommandLineTokenizer(arguments);
      while (argumentsTokenizer.hasMoreTokens()) {
        commandLine.addParameter(argumentsTokenizer.nextToken());
      }
    }
  }
}
