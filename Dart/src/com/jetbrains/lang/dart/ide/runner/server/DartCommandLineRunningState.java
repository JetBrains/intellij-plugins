package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.text.StringTokenizer;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;

public class DartCommandLineRunningState extends CommandLineState {
  private final @NotNull String myFilePath;
  private final @NotNull String myVmOptions;
  private final @NotNull String myArguments;
  private final @NotNull String myWorkingDirectory;

  public DartCommandLineRunningState(final @NotNull ExecutionEnvironment env,
                                     final @NotNull String filePath,
                                     final @NotNull String vmOptions,
                                     final @NotNull String workingDirectory,
                                     final @NotNull String arguments) {
    super(env);
    myFilePath = filePath;
    myVmOptions = vmOptions;
    myWorkingDirectory = workingDirectory;
    myArguments = arguments;

    final TextConsoleBuilder builder = getConsoleBuilder();
    if (builder instanceof TextConsoleBuilderImpl) {
      ((TextConsoleBuilderImpl)builder).setUsePredefinedMessageFilter(false);
    }

    builder.addFilter(new DartConsoleFilter(env.getProject(), LocalFileSystem.getInstance().findFileByPath(filePath)));
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
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      throw new ExecutionException("Dart SDK is not configured");
    }

    final String dartExePath = DartSdkUtil.getDartExePath(sdk);

    final VirtualFile libraryFile = LocalFileSystem.getInstance().findFileByPath(myFilePath);
    if (libraryFile == null) {
      throw new ExecutionException(DartBundle.message("bad.script.path", myFilePath));
    }
    final GeneralCommandLine commandLine = new GeneralCommandLine();

    commandLine.setExePath(dartExePath);
    commandLine.setWorkDirectory(myWorkingDirectory);
    commandLine.setPassParentEnvironment(true);

    setupUserProperties(commandLine, libraryFile);

    return commandLine;
  }

  private void setupUserProperties(final @NotNull GeneralCommandLine commandLine, final @NotNull VirtualFile libraryFile) {
    //commandLine.getEnvironment().put("com.google.dart.sdk", sdk.getSdkPath());

    commandLine.addParameter("--ignore-unrecognized-flags");

    StringTokenizer argumentsTokenizer = new StringTokenizer(myVmOptions);
    while (argumentsTokenizer.hasMoreTokens()) {
      commandLine.addParameter(argumentsTokenizer.nextToken());
    }

    final VirtualFile[] packageRoots = DartUrlResolver.getInstance(getEnvironment().getProject(), libraryFile).getPackageRoots();
    if (packageRoots.length > 0) {
      // more than one package root is not supported by the [SDK]/bin/dart tool
      commandLine.addParameter("--package-root=" + packageRoots[0].getPath());
    }

    commandLine.addParameter(myFilePath);

    argumentsTokenizer = new StringTokenizer(myArguments);
    while (argumentsTokenizer.hasMoreTokens()) {
      commandLine.addParameter(argumentsTokenizer.nextToken());
    }
  }
}
