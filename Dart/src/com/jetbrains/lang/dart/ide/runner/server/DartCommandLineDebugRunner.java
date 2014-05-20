package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

public class DartCommandLineDebugRunner extends DefaultProgramRunner {
  public static final String DART_DEBUG_RUNNER_ID = "DartCommandLineDebugRunner";

  @NotNull
  @Override
  public String getRunnerId() {
    return DART_DEBUG_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof DartCommandLineRunConfiguration;
  }

  @Override
  protected RunContentDescriptor doExecute(final @NotNull Project project,
                                           final @NotNull RunProfileState runProfileState,
                                           final RunContentDescriptor contentToReuse,
                                           final @NotNull ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final DartCommandLineRunConfiguration configuration = (DartCommandLineRunConfiguration)env.getRunProfile();

    final String filePath = configuration.getRunnerParameters().getFilePath();
    if (StringUtil.isEmptyOrSpaces(filePath)) {
      throw new ExecutionException(DartBundle.message("path.to.dart.file.not.set"));
    }

    final VirtualFile mainDartFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (mainDartFile == null) {
      throw new ExecutionException(DartBundle.message("dart.file.not.found", filePath));
    }

    final int debuggingPort = NetUtils.tryToFindAvailableSocketPort();
    final DartCommandLineRunningState state = new DartCommandLineRunningState(env, configuration.getRunnerParameters(), debuggingPort);
    final ExecutionResult executionResult = state.execute(env.getExecutor(), this);

    final XDebugSession debugSession =
      XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
        @Override
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
          return new DartCommandLineDebugProcess(session, debuggingPort, executionResult, mainDartFile);
        }
      });

    return debugSession.getRunContentDescriptor();
  }
}
