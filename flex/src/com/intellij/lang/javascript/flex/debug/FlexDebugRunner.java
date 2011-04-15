package com.intellij.lang.javascript.flex.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.run.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * User: Maxim.Mossienko
 * Date: Mar 11, 2008
 * Time: 8:16:33 PM
 */
public class FlexDebugRunner extends FlexBaseRunner {

  private static final Logger LOG = Logger.getInstance(FlexDebugRunner.class.getName());

  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof FlexRunConfiguration;
  }

  @NotNull
  public String getRunnerId() {
    return "FlexDebugRunner";
  }

  protected RunContentDescriptor doLaunch(final Project project,
                                          final Executor executor,
                                          final RunProfileState state,
                                          final RunContentDescriptor contentToReuse,
                                          final ExecutionEnvironment env,
                                          final Sdk flexSdk,
                                          final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
    if (isRunOnDevice(flexRunnerParameters) &&
        !FlexRunner.packAndInstallToDevice(project, flexSdk, (AirMobileRunnerParameters)flexRunnerParameters, true)) {
      return null;
    }

    final XDebugSession debugSession =
      XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
          try {
            return new FlexDebugProcess(session, flexSdk, flexRunnerParameters) {
              @NotNull
              @Override
              public ExecutionConsole createConsole() {
                if (flexRunnerParameters instanceof FlexUnitRunnerParameters) {
                  try {
                    return createFlexUnitRunnerConsole(project, env, getProcessHandler(), executor);
                  }
                  catch (ExecutionException e) {
                    LOG.error(e);
                  }
                }
                return super.createConsole();
              }
            };
          }
          catch (IOException e) {
            throw new ExecutionException(e.getMessage(), e);
          }
        }
      });

    return debugSession.getRunContentDescriptor();
  }
}
