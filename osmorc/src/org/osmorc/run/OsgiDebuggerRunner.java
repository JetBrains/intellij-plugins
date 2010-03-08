package org.osmorc.run;

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link GenericDebuggerRunner}
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class OsgiDebuggerRunner extends GenericDebuggerRunner {
  private final Logger logger = Logger.getInstance("#org.osmorc.run.OsgiDebuggerRunner");

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
    final RunnerSettings myRunnerSettings = state.getRunnerSettings();

    if (runState.requiresRemoteDebugger()) {
      // this is actually copied from the default, but well
      String myDebugPort = null;
      if (myRunnerSettings.getData() instanceof DebuggingRunnerData) {
        myDebugPort = ((DebuggingRunnerData)myRunnerSettings.getData()).getDebugPort();
        if (myDebugPort.length() == 0) {
          try {
            myDebugPort = DebuggerUtils.getInstance().findAvailableDebugAddress(true);
          }
          catch (ExecutionException e) {
            logger.error(e);
          }
          ((DebuggingRunnerData)myRunnerSettings.getData()).setDebugPort(myDebugPort);
        }
        ((DebuggingRunnerData)myRunnerSettings.getData()).setLocal(false);
      }
      final RemoteConnection connection = new RemoteConnection(true, "127.0.0.1", myDebugPort, true);
      return attachVirtualMachine(project, executor, state, contentToReuse, env, connection, false);
    }
    else {
      // let the default debugger do it's job.
      return super.createContentDescriptor(project, executor, state, contentToReuse, env);
    }
  }
}
