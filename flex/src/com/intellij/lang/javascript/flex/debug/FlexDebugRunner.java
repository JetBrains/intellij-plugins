package com.intellij.lang.javascript.flex.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitCommonParameters;
import com.intellij.lang.javascript.flex.flexunit.NewFlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.NewFlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.run.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileDebugTransport;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileRunTarget;

/**
 * User: Maxim.Mossienko
 * Date: Mar 11, 2008
 * Time: 8:16:33 PM
 */
public class FlexDebugRunner extends FlexBaseRunner {

  private static final Logger LOG = Logger.getInstance(FlexDebugRunner.class.getName());

  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof FlashRunConfiguration ||
            profile instanceof NewFlexUnitRunConfiguration ||
            profile instanceof RemoteFlashRunConfiguration);
  }

  @NotNull
  public String getRunnerId() {
    return "FlexDebugRunner";
  }

  protected RunContentDescriptor launchWebFlexUnit(final Project project,
                                                   final Executor executor,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final FlexUnitCommonParameters params,
                                                   final String swfFilePath) throws ExecutionException {
    try {
      final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC = ((NewFlexUnitRunnerParameters)params).checkAndGetModuleAndBC(project);
      return launchDebugProcess(moduleAndBC.first, moduleAndBC.second, (NewFlexUnitRunnerParameters)params, executor, contentToReuse, env);
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }
  }

  protected RunContentDescriptor launchAirFlexUnit(final Project project,
                                                   final Executor executor,
                                                   final RunProfileState state,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final FlexUnitCommonParameters params) throws ExecutionException {
    try {
      final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC = ((NewFlexUnitRunnerParameters)params).checkAndGetModuleAndBC(project);
      return launchDebugProcess(moduleAndBC.first, moduleAndBC.second, (NewFlexUnitRunnerParameters)params, executor, contentToReuse, env);
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }
  }

  protected RunContentDescriptor launchFlexIdeConfig(final Module module,
                                                     final FlexIdeBuildConfiguration bc,
                                                     final FlashRunnerParameters params,
                                                     final Executor executor,
                                                     final RunProfileState state,
                                                     final RunContentDescriptor contentToReuse,
                                                     final ExecutionEnvironment env) throws ExecutionException {
    final Project project = module.getProject();

    if (bc.getTargetPlatform() == TargetPlatform.Mobile && params.getMobileRunTarget() == AirMobileRunTarget.AndroidDevice) {
      final Sdk flexSdk = bc.getSdk();
      final String appId = getApplicationId(getAirDescriptorPath(bc, bc.getAndroidPackagingOptions()));

      final MobileAirPackageParameters packageParameters = createAndroidPackageParams(bc, params, true);

      if (!packAndInstallToAndroidDevice(module, flexSdk, packageParameters, appId, true)) {
        return null;
      }

      if (params.getDebugTransport() == AirMobileDebugTransport.USB) {
        launchOnAndroidDevice(project, flexSdk, appId, true);
        waitUntilCountdownStartsOnDevice(project, appId);
        MobileAirUtil.forwardTcpPort(project, flexSdk, params.getUsbDebugPort());
      }
    }

    return launchDebugProcess(module, bc, params, executor, contentToReuse, env);
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
