package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import com.intellij.util.text.StringTokenizer;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.DartStackTraceMessageFilter;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartSdkUtil;
import org.jetbrains.annotations.NotNull;

public class DartCommandLineRunningState extends CommandLineState {
  private final Module module;
  private final @NotNull String filePath;
  private final @NotNull String vmOptions;
  private final @NotNull String arguments;

  public DartCommandLineRunningState(ExecutionEnvironment env,
                                     Module module,
                                     @NotNull String filePath,
                                     @NotNull String vmOptions,
                                     @NotNull String arguments) {
    super(env);
    this.module = module;
    this.filePath = filePath;
    this.vmOptions = vmOptions;
    this.arguments = arguments;
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
    DartSettings dartSettings = DartSettings.getSettingsForModule(module);
    String dartExecutablePath = DartSdkUtil.getCompilerPathByFolderPath(dartSettings != null ? dartSettings.getSdkPath() : null);
    if (dartSettings == null || dartExecutablePath == null) {
      throw new ExecutionException(DartBundle.message("bad.home.for.sdk", module.getName()));
    }
    final VirtualFile libraryFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(filePath));
    if (libraryFile == null) {
      throw new ExecutionException(DartBundle.message("bad.script.path", filePath));
    }
    final GeneralCommandLine commandLine = new GeneralCommandLine();

    commandLine.setExePath(dartExecutablePath);
    commandLine.setWorkDirectory(PathUtil.getParentPath(filePath));
    commandLine.setPassParentEnvironment(true);

    setupUserProperties(
      libraryFile,
      commandLine,
      dartSettings
    );

    addConsoleFilters(new DartStackTraceMessageFilter(module.getProject(), filePath));

    return commandLine;
  }

  private void setupUserProperties(VirtualFile libraryFile,
                                   GeneralCommandLine commandLine,
                                   @NotNull DartSettings sdk) {
    commandLine.getEnvironment().put("com.google.dart.sdk", sdk.getSdkPath());

    commandLine.addParameter("--ignore-unrecognized-flags");

    StringTokenizer argumentsTokenizer = new StringTokenizer(vmOptions);
    while (argumentsTokenizer.hasMoreTokens()) {
      commandLine.addParameter(argumentsTokenizer.nextToken());
    }

    final VirtualFile packages = DartResolveUtil.getDartPackagesFolder(module.getProject(), libraryFile);
    if (packages != null && packages.isDirectory()) {
      commandLine.addParameter("--package-root=" + packages.getPath() + "/");
    }

    commandLine.addParameter(filePath);

    argumentsTokenizer = new StringTokenizer(arguments);
    while (argumentsTokenizer.hasMoreTokens()) {
      commandLine.addParameter(argumentsTokenizer.nextToken());
    }
  }
}
