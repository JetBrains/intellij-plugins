package org.jetbrains.flutter.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunningState;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class FlutterRunningState extends DartCommandLineRunningState {
  private int myObservatoryPort = 8181; // TODO Make this a parameter.

  public FlutterRunningState(@NotNull ExecutionEnvironment environment) throws ExecutionException {
    super(environment);
  }

  public FlutterRunnerParameters params() {
    return (FlutterRunnerParameters)myRunnerParameters;
  }

  protected ProcessHandler doStartProcess(final @Nullable String overriddenMainFilePath) throws ExecutionException {
    final GeneralCommandLine commandLine = createCommandLine(overriddenMainFilePath);
    final OSProcessHandler processHandler = new ColoredProcessHandler(commandLine);
    ProcessTerminatedListener.attach(processHandler, getEnvironment().getProject());
    return processHandler;
  }

  private GeneralCommandLine createCommandLine(final @Nullable String overriddenMainFilePath) throws ExecutionException {
    DartSdk sdk = DartSdk.getDartSdk(getEnvironment().getProject());
    if (sdk == null) {
      throw new ExecutionException(DartBundle.message("dart.sdk.is.not.configured"));
    }

    FlutterRunnerParameters params = (FlutterRunnerParameters)myRunnerParameters;
    String flutterSdkPath = params.getFlutterSdkPath();
    flutterSdkPath = verifyFlutterSdk(flutterSdkPath);
    VirtualFile projectDir = flutterProjectDir(params.getFilePath());

    String workingDir = projectDir.getCanonicalPath();
    String flutterExec = pathToFlutter(flutterSdkPath);

    final GeneralCommandLine commandLine = new GeneralCommandLine().withWorkDirectory(workingDir);
    commandLine.setCharset(CharsetToolkit.UTF8_CHARSET);
    commandLine.setExePath(FileUtil.toSystemDependentName(flutterExec));
    commandLine.getEnvironment().putAll(myRunnerParameters.getEnvs());
    commandLine.withParentEnvironmentType(myRunnerParameters.isIncludeParentEnvs()
                                          ? GeneralCommandLine.ParentEnvironmentType.CONSOLE
                                          : GeneralCommandLine.ParentEnvironmentType.NONE);
    commandLine.addParameter("logs");
    commandLine.addParameter("--clear");

    return commandLine;
  }

  @NotNull
  public static String verifyFlutterSdk(@Nullable String path) throws ExecutionException {
    if (path == null || path.isEmpty()) {
      throw new ExecutionException("No flutter sdk given"); // TODO Externalize strings
    }
    File flutterSdk = new File(path);
    File bin = new File(flutterSdk, "bin");
    if (!bin.isDirectory()) {
      throw new ExecutionException("No flutter sdk given"); // TODO Externalize strings
    }
    if (!(new File(bin, "flutter").exists())) {
      throw new ExecutionException("No flutter sdk given"); // TODO Externalize strings
    }
    return path;
  }

  @NotNull
  public static String pathToFlutter(@NotNull String sdkPath) throws ExecutionException {
    // The exception is not thrown if this follows the call to verifyFlutterSdk().
    VirtualFile sdk = LocalFileSystem.getInstance().findFileByPath(sdkPath);
    if (sdk == null) throw new ExecutionException("No flutter sdk given"); // TODO Externalize strings
    VirtualFile bin = sdk.findChild("bin");
    if (bin == null) throw new ExecutionException("No flutter sdk given"); // TODO Externalize strings
    VirtualFile exec = bin.findChild("flutter"); // TODO Use flutter.bat on Windows
    if (exec == null) throw new ExecutionException("No flutter sdk given"); // TODO Externalize strings
    return exec.getPath();
  }

  @NotNull
  public static VirtualFile flutterProjectDir(@Nullable String projectPath) throws ExecutionException {
    if (projectPath == null) throw new ExecutionException("Project directory not given"); // TODO Externalize strings
    VirtualFile projectDir = LocalFileSystem.getInstance().findFileByPath(projectPath);
    while (projectDir != null) {
      if (projectDir.isDirectory() && projectDir.findChild("pubspec.yaml") != null) {
        return projectDir;
      }
      projectDir = projectDir.getParent();
    }
    throw new ExecutionException("Project directory not given"); // TODO Externalize strings
  }

  public int getObservatoryPort() {
    return myObservatoryPort;
  }
}
