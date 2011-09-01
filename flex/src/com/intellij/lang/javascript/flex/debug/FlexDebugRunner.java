package com.intellij.lang.javascript.flex.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.run.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Pair;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.AirMobileDebugTransport;

/**
 * User: Maxim.Mossienko
 * Date: Mar 11, 2008
 * Time: 8:16:33 PM
 */
public class FlexDebugRunner extends FlexBaseRunner {

  private static final Logger LOG = Logger.getInstance(FlexDebugRunner.class.getName());

  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof FlexRunConfiguration || profile instanceof FlexIdeRunConfiguration);
  }

  @NotNull
  public String getRunnerId() {
    return "FlexDebugRunner";
  }

  protected RunContentDescriptor launchFlexIdeConfig(final Module module,
                                                     final FlexIdeBuildConfiguration config,
                                                     final FlexIdeRunnerParameters params,
                                                     final Executor executor,
                                                     final RunContentDescriptor contentToReuse,
                                                     final ExecutionEnvironment env) throws ExecutionException  {
    if (config.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Web) {
      final Project project = module.getProject();
      final XDebugSession debugSession =
        XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
          @NotNull
          public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
            try {
              return new FlexDebugProcess(session, config, params);
            }
            catch (IOException e) {
              throw new ExecutionException(e.getMessage(), e);
            }
          }
        });

      return debugSession.getRunContentDescriptor();
    }

    // todo implement
    return null;
  }

  protected RunContentDescriptor doLaunch(final Project project,
                                          final Executor executor,
                                          final RunProfileState state,
                                          final RunContentDescriptor contentToReuse,
                                          final ExecutionEnvironment env,
                                          final Sdk flexSdk,
                                          final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
    if (isRunOnDevice(flexRunnerParameters)) {
      final AirMobileRunnerParameters mobileParams = (AirMobileRunnerParameters)flexRunnerParameters;
      final String appId;

      if (mobileParams.getAirMobileRunMode() == AirMobileRunnerParameters.AirMobileRunMode.ExistingPackage) {
        appId = MobileAirUtil.getAppIdFromPackage(mobileParams.getExistingPackagePath());
        if (appId == null) {
          throw new ExecutionException("Failed to get application id for package: " + mobileParams.getExistingPackagePath());
        }
        if (!installToDevice(project, flexSdk, mobileParams, appId)) {
          return null;
        }
      }
      else {
        final Pair<String, String> swfPathAndApplicationId = getSwfPathAndApplicationId(mobileParams);
        appId = swfPathAndApplicationId.second;

        if (!packAndInstallToDevice(project, flexSdk, mobileParams, swfPathAndApplicationId.first, swfPathAndApplicationId.second, true)) {
          return null;
        }
      }

      if (mobileParams.getDebugTransport() == AirMobileDebugTransport.USB) {
        launchOnDevice(project, flexSdk, mobileParams, appId, true);
        waitUntilCountdownStartsOnDevice(project, appId);
        MobileAirUtil.forwardTcpPort(project, flexSdk, mobileParams.getUsbDebugPort());
      }
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

  /**
   * While debug mobile application over USB fdb must be launched only when Android AIR app is started and shows countdown.
   * So this method waits for some time (3 seconds by default) as we hope that it is enough for application for startup and do not want to bother user with a dialog.
   */
  private static void waitUntilCountdownStartsOnDevice(final Project project, final String applicationId) {
    final Runnable process = new Runnable() {
      public void run() {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.setIndeterminate(true);
        }

        try {
          Thread.sleep(Integer.getInteger("air.mobile.application.startup.waiting.time.millis", 3000));
        }
        catch (InterruptedException ignored) {/*ignore*/}
      }
    };

    ProgressManager.getInstance().runProcessWithProgressSynchronously(process, FlexBundle.message("waiting.for.application.startup"), false,
                                                                      project);
  }
}
