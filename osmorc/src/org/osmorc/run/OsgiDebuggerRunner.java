package org.osmorc.run;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.debugger.impl.GenericDebuggerRunnerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link GenericDebuggerRunner}
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class OsgiDebuggerRunner extends GenericDebuggerRunner {

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) && profile instanceof OsgiRunConfiguration;
  }

  @Override
  protected RunContentDescriptor createContentDescriptor(Project project,
                                                         Executor executor,
                                                         RunProfileState state,
                                                         RunContentDescriptor contentToReuse,
                                                         ExecutionEnvironment env) throws ExecutionException {
    OsgiRunState runState = (OsgiRunState)state;

    if (runState.requiresRemoteDebugger()) {
      // this is actually copied from the default, but well 
      final RemoteConnection connection = createRemoteDebugConnection((RemoteState)state, state.getRunnerSettings());
      return attachVirtualMachine(project, executor, state, contentToReuse, env, connection, false);

    }
    else {
      // let the default debugger do it's job.
      return super.createContentDescriptor(project, executor, state, contentToReuse, env);
    }
  }

  private static RemoteConnection createRemoteDebugConnection(RemoteState connection, final RunnerSettings settings) {
     final RemoteConnection remoteConnection = connection.getRemoteConnection();

     GenericDebuggerRunnerSettings debuggerRunnerSettings = ((GenericDebuggerRunnerSettings)settings.getData());

     if (debuggerRunnerSettings != null) {
//       remoteConnection.setUseSockets(debuggerRunnerSettings.getTransport() == DebuggerSettings.SOCKET_TRANSPORT);
//       remoteConnection.setAddress(debuggerRunnerSettings.DEBUG_PORT);
     }

     return remoteConnection;
   }
}
