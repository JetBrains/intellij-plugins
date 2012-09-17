package com.intellij.lang.javascript.flex.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageUtil;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.lang.javascript.flex.run.FlexBaseRunner;
import com.intellij.lang.javascript.flex.run.RemoteFlashRunConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileDebugTransport;

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
            profile instanceof FlexUnitRunConfiguration ||
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
                                                   final FlexUnitRunnerParameters params,
                                                   final String swfFilePath) throws ExecutionException {
    try {
      final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC = ((FlexUnitRunnerParameters)params).checkAndGetModuleAndBC(project);
      return launchDebugProcess(moduleAndBC.first, moduleAndBC.second, (FlexUnitRunnerParameters)params, executor, contentToReuse, env);
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
                                                   final FlexUnitRunnerParameters params) throws ExecutionException {
    try {
      final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC = params.checkAndGetModuleAndBC(project);
      return launchDebugProcess(moduleAndBC.first, moduleAndBC.second, params, executor, contentToReuse, env);
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }
  }

  protected RunContentDescriptor launchFlexIdeConfig(final Module module,
                                                     final FlexIdeBuildConfiguration bc,
                                                     final FlashRunnerParameters runnerParameters,
                                                     final Executor executor,
                                                     final RunProfileState state,
                                                     final RunContentDescriptor contentToReuse,
                                                     final ExecutionEnvironment env) throws ExecutionException {
    final Project project = module.getProject();

    if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      final Sdk sdk = bc.getSdk();

      switch (runnerParameters.getMobileRunTarget()) {
        case Emulator:
          break;
        case AndroidDevice:
          final String androidAppId = getApplicationId(getAirDescriptorPath(bc, bc.getAndroidPackagingOptions()));

          if (!packAndInstallToAndroidDevice(module, bc, runnerParameters, androidAppId, true)) {
            return null;
          }

          if (runnerParameters.getDebugTransport() == AirMobileDebugTransport.USB) {
            if (!AirPackageUtil.androidForwardTcpPort(project, sdk, runnerParameters.getUsbDebugPort())) {
              return null;
            }
          }

          break;
        case iOSSimulator:
          final String iosSimulatorAppId = getApplicationId(getAirDescriptorPath(bc, bc.getIosPackagingOptions()));

          if (!packAndInstallToIOSSimulator(module, bc, runnerParameters, iosSimulatorAppId, true)) {
            return null;
          }

          break;
        case iOSDevice:
          if (!packAndInstallToIOSDevice(module, bc, runnerParameters, true)) {
            return null;
          }

          if (runnerParameters.getDebugTransport() == AirMobileDebugTransport.USB) {
            final int deviceHandle = AirPackageUtil.getIOSDeviceHandle(project, sdk);
            if (deviceHandle < 0) {
              return null;
            }

            if (!AirPackageUtil.iosForwardTcpPort(project, sdk, runnerParameters.getUsbDebugPort(), deviceHandle)) {
              return null;
            }
          }

          break;
      }
    }

    return launchDebugProcess(module, bc, runnerParameters, executor, contentToReuse, env);
  }
}
