package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;

public class DartUnitDebugRunner extends DefaultProgramRunner {
  public static final String DART_UNIT_DEBUG_RUNNER_ID = "DartUnitDebugRunner";

  @NotNull
  @Override
  public String getRunnerId() {
    return DART_UNIT_DEBUG_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof DartUnitRunConfiguration;
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull Project project,
                                           @NotNull RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           @NotNull ExecutionEnvironment env) throws ExecutionException {
    final DartUnitRunConfiguration configuration = (DartUnitRunConfiguration)env.getRunProfile();

    final DartUnitRunnerParameters parameters = configuration.getRunnerParameters();
    final String filePath = parameters.getFilePath();
    assert filePath != null;

    final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(filePath));
    if (virtualFile == null) {
      throw new ExecutionException("Can't find file: " + filePath);
    }

    final int debuggingPort = NetUtils.tryToFindAvailableSocketPort();

    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      throw new ExecutionException("Dart SDK is not configured");
    }

    final DartUnitRunningState dartUnitRunningState = new DartUnitRunningState(env, parameters, sdk, debuggingPort);
    final ExecutionResult executionResult = dartUnitRunningState.execute(env.getExecutor(), this);

    final XDebugSession debugSession =
      XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
        @Override
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
          return new DartCommandLineDebugProcess(session, debuggingPort, executionResult, filePath);
        }
      });

    return debugSession.getRunContentDescriptor();
  }
}
