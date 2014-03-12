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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
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
  protected RunContentDescriptor doExecute(@NotNull Project project,
                                           @NotNull RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           @NotNull ExecutionEnvironment env) throws ExecutionException {
    final DartCommandLineRunConfiguration configuration = (DartCommandLineRunConfiguration)env.getRunProfile();
    FileDocumentManager.getInstance().saveAllDocuments();
    final Module module = configuration.getModule();

    final String filePath = configuration.getFilePath();
    assert filePath != null;

    final int debuggingPort = NetUtils.tryToFindAvailableSocketPort();

    final DartCommandLineRunningState dartCommandLineRunningState = new DartCommandLineRunningState(
      env,
      module,
      filePath,
      ("--debug:" + debuggingPort) + " " + StringUtil.notNullize(configuration.getVMOptions()),
      StringUtil.notNullize(configuration.getArguments())
    );

    final ExecutionResult executionResult = dartCommandLineRunningState.execute(env.getExecutor(), this);

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
