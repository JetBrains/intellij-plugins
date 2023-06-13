// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageUtil;
import com.intellij.lang.javascript.flex.actions.airpackage.DeviceInfo;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.lang.javascript.flex.run.FlexBaseRunner;
import com.intellij.lang.javascript.flex.run.RemoteFlashRunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileDebugTransport;

public class FlexDebugRunner extends FlexBaseRunner {

  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof FlashRunConfiguration ||
            profile instanceof FlexUnitRunConfiguration ||
            profile instanceof RemoteFlashRunConfiguration);
  }

  @Override
  @NotNull
  public String getRunnerId() {
    return "FlexDebugRunner";
  }

  @Override
  protected RunContentDescriptor launchWebFlexUnit(final Project project,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final FlexUnitRunnerParameters params,
                                                   final String swfFilePath) throws ExecutionException {
    try {
      final Pair<Module, FlexBuildConfiguration> moduleAndBC = params.checkAndGetModuleAndBC(project);
      return launchDebugProcess(moduleAndBC.first, moduleAndBC.second, params, env);
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }
  }

  @Override
  protected RunContentDescriptor launchAirFlexUnit(final Project project,
                                                   final RunProfileState state,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final FlexUnitRunnerParameters params) throws ExecutionException {
    try {
      final Pair<Module, FlexBuildConfiguration> moduleAndBC = params.checkAndGetModuleAndBC(project);
      return launchDebugProcess(moduleAndBC.first, moduleAndBC.second, params, env);
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }
  }

  @Override
  protected RunContentDescriptor launchFlexConfig(final Module module,
                                                  final FlexBuildConfiguration bc,
                                                  final FlashRunnerParameters runnerParameters,
                                                  final RunProfileState state,
                                                  final RunContentDescriptor contentToReuse,
                                                  final ExecutionEnvironment env) throws ExecutionException {
    final Project project = module.getProject();

    if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      final Sdk sdk = bc.getSdk();

      switch (runnerParameters.getMobileRunTarget()) {
        case Emulator -> { }
        case AndroidDevice -> {
          final String androidDescriptorPath = getAirDescriptorPath(bc, bc.getAndroidPackagingOptions());
          final String androidAppId = getApplicationId(androidDescriptorPath);

          if (androidAppId == null) {
            Messages.showErrorDialog(project,
                                     FlexBundle.message("failed.to.read.app.id", FileUtil.toSystemDependentName(androidDescriptorPath)),
                                     FlexBundle.message("error.title"));
            return null;
          }

          if (!packAndInstallToAndroidDevice(module, bc, runnerParameters, androidAppId, true)) {
            return null;
          }

          if (runnerParameters.getDebugTransport() == AirMobileDebugTransport.USB) {
            if (!AirPackageUtil.androidForwardTcpPort(project, sdk, runnerParameters.getDeviceInfo(), runnerParameters.getUsbDebugPort())) {
              return null;
            }
          }
        }
        case iOSSimulator -> {
          final String adtVersionSimulator = AirPackageUtil.getAdtVersion(module.getProject(), bc.getSdk());

          final String iosSimulatorDescriptorPath = getAirDescriptorPath(bc, bc.getIosPackagingOptions());
          final String iosSimulatorAppId = getApplicationId(iosSimulatorDescriptorPath);

          if (iosSimulatorAppId == null) {
            Messages.showErrorDialog(project, FlexBundle.message("failed.to.read.app.id",
                                                                 FileUtil.toSystemDependentName(iosSimulatorDescriptorPath)),
                                     FlexBundle.message("error.title"));
            return null;
          }

          if (!packAndInstallToIOSSimulator(module, bc, runnerParameters, adtVersionSimulator, iosSimulatorAppId, true)) {
            return null;
          }
        }
        case iOSDevice -> {
          final String adtVersion = AirPackageUtil.getAdtVersion(module.getProject(), bc.getSdk());

          if (StringUtil.compareVersionNumbers(adtVersion, "3.4") >= 0) {
            if (!packAndInstallToIOSDevice(module, bc, runnerParameters, adtVersion, true)) {
              return null;
            }

            if (runnerParameters.getDebugTransport() == AirMobileDebugTransport.USB) {
              final DeviceInfo device = runnerParameters.getDeviceInfo();
              final int deviceHandle = device == null ? -1 : device.IOS_HANDLE;
              if (deviceHandle < 0) {
                return null;
              }

              if (!AirPackageUtil.iosForwardTcpPort(project, sdk, runnerParameters.getUsbDebugPort(), deviceHandle)) {
                return null;
              }
            }
          }
          else {
            if (!AirPackageUtil.packageIpaForDevice(module, bc, runnerParameters, adtVersion, true)) {
              return null;
            }
          }
        }
      }
    }

    return launchDebugProcess(module, bc, runnerParameters, env);
  }
}
