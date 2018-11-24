package org.jetbrains.flutter.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.ide.runner.DartRunner;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import com.jetbrains.lang.dart.util.DartUrlResolverImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

import static org.jetbrains.flutter.run.FlutterRunningState.*;

public class FlutterRunner extends DartRunner {
  private static final Logger LOG = Logger.getInstance(FlutterRunner.class);

  @NotNull
  @Override
  public String getRunnerId() {
    return "FlutterRunner";
  }

  @Override
  public boolean canRun(final @NotNull String executorId, final @NotNull RunProfile profile) {
    return (profile instanceof FlutterRunConfiguration &&
            (DefaultRunExecutor.EXECUTOR_ID.equals(executorId) || DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)));
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
    GeneralCommandLine line = createCommandLine(((FlutterRunningState)state).params(), env.getExecutor().getActionName());
    ProcessOutput output = ExecUtil.execAndGetOutput(line);
    return output.checkSuccess(LOG) ? super.doExecute(state, env) : null;
  }

  private static GeneralCommandLine createCommandLine(FlutterRunnerParameters params, String actionName) throws ExecutionException {
    String flutterSdkPath = params.getFlutterSdkPath();
    flutterSdkPath = verifyFlutterSdk(flutterSdkPath);
    VirtualFile projectDir = flutterProjectDir(params.getFilePath());
    String workingDir = projectDir.getCanonicalPath();
    String flutterExec = pathToFlutter(flutterSdkPath);

    GeneralCommandLine commandLine = new GeneralCommandLine().withWorkDirectory(workingDir);
    commandLine.setCharset(CharsetToolkit.UTF8_CHARSET);
    commandLine.setExePath(FileUtil.toSystemDependentName(flutterExec));
    commandLine.addParameter("start");
    commandLine.addParameter("--checked");
    if ("Debug".equals(actionName)) {
      commandLine.addParameter("--start-paused");
    }

    return commandLine;
  }

  protected DartUrlResolver getDartUrlResolver(@NotNull final Project project, @NotNull final VirtualFile contextFileOrDir) {
    return new FlutterUrlResolver(project, contextFileOrDir);
  }

  private static class FlutterUrlResolver extends DartUrlResolverImpl {
    private static final String PACKAGE_PREFIX = "package:";
    //private static final String PACKAGES_PREFIX = "packages/";

    FlutterUrlResolver(final @NotNull Project project, final @NotNull VirtualFile contextFile) {
      super(project, contextFile);
    }

    public boolean mayNeedDynamicUpdate() {
      return false;
    }

    @NotNull
    public String getDartUrlForFile(final @NotNull VirtualFile file) {
      String str = super.getDartUrlForFile(file);
      if (str.startsWith(PACKAGE_PREFIX)) {
        // Convert package: prefix to packages/ one.
        //return PACKAGES_PREFIX + str.substring(PACKAGE_PREFIX.length());
        return file.getPath(); // TODO This works on Mac running flutter locally. Not sure about other configs.
      }
      else if (str.startsWith("file:")) {
        return URI.create(str).toString();
      }
      return str;
    }

    @Nullable
    public VirtualFile findFileByDartUrl(final @NotNull String url) {
      VirtualFile file = super.findFileByDartUrl(url);
      if (file == null) {
        file = LocalFileSystem.getInstance().findFileByPath(SystemInfo.isWindows ? url : ("/" + url));
      }
      return file;
    }
  }
}
