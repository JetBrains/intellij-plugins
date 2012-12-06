package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.chromeConnector.connection.ChromeConnection;
import com.intellij.chromeConnector.connection.ChromeConnectionManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.javascript.debugger.impl.DefaultDebuggableFileFinder;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;


/**
 * @author: Fedor.Korotkov
 */
public class DartiumDebugRunner extends DefaultProgramRunner {
  public static final String DART_DEBUG_RUNNER_ID = "DartDebugRunner";

  @NotNull
  @Override
  public String getRunnerId() {
    return DART_DEBUG_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof DartDebugConfigurationBase;
  }

  @Override
  protected RunContentDescriptor doExecute(Project project,
                                           Executor executor,
                                           RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
    final DartDebugConfigurationBase configuration = (DartDebugConfigurationBase)env.getRunProfile();
    final ChromeConnection connection = ChromeConnectionManager.getInstance().createConnection(true);

    FileDocumentManager.getInstance().saveAllDocuments();
    final String url = configuration.getFileUrl();
    final DebuggableFileFinder fileFinder = getFileFinder(project, configuration);
    final XDebugSession debugSession =
      XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) {
          return new DartiumDebugProcess(session, fileFinder, connection, url);
        }
      });

    return debugSession.getRunContentDescriptor();
  }

  private static DebuggableFileFinder getFileFinder(Project project, DartDebugConfigurationBase configuration) {
    if (configuration instanceof LocalDartDebugConfiguration) {
      return DefaultDebuggableFileFinder.INSTANCE;
    }
    final RemoteDartDebugConfiguration remoteDartDebugConfiguration = (RemoteDartDebugConfiguration)configuration;
    return new RemoteDebuggingFileFinder(project, remoteDartDebugConfiguration.getMappings());
  }
}
