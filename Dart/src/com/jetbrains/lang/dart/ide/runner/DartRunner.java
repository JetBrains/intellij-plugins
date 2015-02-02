package com.jetbrains.lang.dart.ide.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunningState;
import com.jetbrains.lang.dart.ide.runner.server.DartRemoteDebugConfiguration;
import org.jetbrains.annotations.NotNull;

public class DartRunner extends DefaultProgramRunner {

  private static final Logger LOG = Logger.getInstance(DartRunner.class.getName());

  @NotNull
  @Override
  public String getRunnerId() {
    return "DartRunner";
  }

  @Override
  public boolean canRun(final @NotNull String executorId, final @NotNull RunProfile profile) {
    return (profile instanceof DartRunConfigurationBase && (DefaultRunExecutor.EXECUTOR_ID.equals(executorId) ||
                                                            DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)))
           ||
           (profile instanceof DartRemoteDebugConfiguration && DefaultDebugExecutor.EXECUTOR_ID.equals(executorId));
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final String executorId = env.getExecutor().getId();

    if (DefaultRunExecutor.EXECUTOR_ID.equals(executorId)) {
      return super.doExecute(state, env);
    }

    if (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)) {
      try {
        return doExecuteDartDebug(state, env);
      }
      catch (RuntimeConfigurationError e) {
        throw new ExecutionException(e);
      }
    }

    LOG.error("Unexpected executor id: " + executorId);
    return null;
  }

  private RunContentDescriptor doExecuteDartDebug(final @NotNull RunProfileState state,
                                                  final @NotNull ExecutionEnvironment env) throws RuntimeConfigurationError,
                                                                                                  ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final RunProfile profile = env.getRunProfile();
    if (profile instanceof DartRemoteDebugConfiguration) {
      return null; // todo
    }

    if (profile instanceof DartRunConfigurationBase) {
      final DartRunConfigurationBase configuration = (DartRunConfigurationBase)profile;
      final VirtualFile mainDartFile = configuration.getRunnerParameters().getDartFile();

      final ExecutionResult executionResult = state.execute(env.getExecutor(), this);
      if (executionResult == null) return null;

      final XDebuggerManager debuggerManager = XDebuggerManager.getInstance(env.getProject());
      final XDebugSession debugSession = debuggerManager.startSession(env, new XDebugProcessStarter() {
        @Override
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) {
          return new DartCommandLineDebugProcess(session, (DartCommandLineRunningState)state, executionResult, mainDartFile);
        }
      });

      return debugSession.getRunContentDescriptor();
    }

    LOG.error("Unexpected run configuration: " + profile.getClass().getName());
    return null;
  }
}
