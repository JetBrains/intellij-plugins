// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageUtil;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitConnection;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.flexunit.SwfPolicyFileConnection;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.PathUtil;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.File;

public class FlexRunner extends FlexBaseRunner {
  @Override
  protected RunContentDescriptor launchFlexConfig(final Module module,
                                                  final FlexBuildConfiguration bc,
                                                  final FlashRunnerParameters runnerParameters,
                                                  final RunProfileState state,
                                                  final RunContentDescriptor contentToReuse,
                                                  final ExecutionEnvironment environment) throws ExecutionException {
    switch (bc.getTargetPlatform()) {
      case Web:
        final String urlOrPath = runnerParameters.isLaunchUrl()
                                 ? runnerParameters.getUrl()
                                 : bc.isUseHtmlWrapper()
                                   ? PathUtil.getParentPath(bc.getActualOutputFilePath()) + "/" + BCUtils.getWrapperFileName(bc)
                                   : bc.getActualOutputFilePath();
        launchWithSelectedApplication(urlOrPath, runnerParameters.getLauncherParameters());
        break;
      case Desktop:
        return standardLaunch(module.getProject(), state, contentToReuse, environment);
      case Mobile:
        switch (runnerParameters.getMobileRunTarget()) {
          case Emulator:
            return standardLaunch(module.getProject(), state, contentToReuse, environment);
          case AndroidDevice:
            final String androidDescriptorPath = getAirDescriptorPath(bc, bc.getAndroidPackagingOptions());
            final String androidAppId = getApplicationId(androidDescriptorPath);

            if (androidAppId == null) {
              Messages.showErrorDialog(module.getProject(),
                                       FlexBundle.message("failed.to.read.app.id", FileUtil.toSystemDependentName(androidDescriptorPath)),
                                       FlexBundle.message("error.title"));
              return null;
            }

            if (packAndInstallToAndroidDevice(module, bc, runnerParameters, androidAppId, false)) {
              launchOnAndroidDevice(module.getProject(), bc.getSdk(), runnerParameters.getDeviceInfo(), androidAppId, false);
            }
            return null;
          case iOSSimulator:
            final String adtVersionSimulator = AirPackageUtil.getAdtVersion(module.getProject(), bc.getSdk());

            final String iosSimulatorDescriptorPath = getAirDescriptorPath(bc, bc.getIosPackagingOptions());
            final String iosSimulatorAppId = getApplicationId(iosSimulatorDescriptorPath);

            if (iosSimulatorAppId == null) {
              Messages.showErrorDialog(module.getProject(), FlexBundle.message("failed.to.read.app.id",
                                                                               FileUtil.toSystemDependentName(iosSimulatorDescriptorPath)),
                                       FlexBundle.message("error.title"));
              return null;
            }
            if (packAndInstallToIOSSimulator(module, bc, runnerParameters, adtVersionSimulator, iosSimulatorAppId, false)) {
              launchOnIosSimulator(module.getProject(), bc.getSdk(), iosSimulatorAppId, runnerParameters.getIOSSimulatorSdkPath(),
                                   runnerParameters.getIOSSimulatorDevice(), false);
            }
            return null;
          case iOSDevice:
            final String adtVersion = AirPackageUtil.getAdtVersion(module.getProject(), bc.getSdk());

            if (StringUtil.compareVersionNumbers(adtVersion, "3.4") >= 0) {
              if (packAndInstallToIOSDevice(module, bc, runnerParameters, adtVersion, false)) {
                final String appName = getApplicationName(getAirDescriptorPath(bc, bc.getIosPackagingOptions()));
                ToolWindowManager.getInstance(module.getProject())
                  .notifyByBalloon(ToolWindowId.RUN, MessageType.INFO, FlexBundle.message("ios.application.installed.to.run", appName));
              }
            }
            else {
              if (AirPackageUtil.packageIpaForDevice(module, bc, runnerParameters, adtVersion, false)) {
                final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
                final String ipaName = bc.getIosPackagingOptions().getPackageFileName() + ".ipa";

                final String message = FlexBundle.message("ios.application.packaged.to.run", ipaName);
                ToolWindowManager.getInstance(module.getProject())
                  .notifyByBalloon(ToolWindowId.RUN, MessageType.INFO, message, null, new HyperlinkAdapter() {
                    @Override
                    protected void hyperlinkActivated(final @NotNull HyperlinkEvent e) {
                      RevealFileAction.openFile(new File(outputFolder + "/" + ipaName));
                    }
                  });
              }
            }
            break;
        }
        break;
    }
    return null;
  }

  @Nullable
  private RunContentDescriptor standardLaunch(final Project project,
                                              final RunProfileState state,
                                              final RunContentDescriptor contentToReuse, final ExecutionEnvironment environment)
    throws ExecutionException {
    final ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
    if (executionResult == null) return null;

    final RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, environment);
    return contentBuilder.showRunContent(contentToReuse);
  }

  @Override
  protected RunContentDescriptor launchWebFlexUnit(final Project project,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final FlexUnitRunnerParameters params,
                                                   final String swfFilePath) throws ExecutionException {
    final SwfPolicyFileConnection policyFileConnection = new SwfPolicyFileConnection();
    policyFileConnection.open(params.getSocketPolicyPort());

    final FlexUnitConnection flexUnitConnection = new FlexUnitConnection();
    flexUnitConnection.open(params.getPort());
    final ProcessHandler processHandler = new DefaultDebugProcessHandler() {
      @Override
      protected void destroyProcessImpl() {
        flexUnitConnection.write("Finish");
        flexUnitConnection.close();

        policyFileConnection.close();
        super.destroyProcessImpl();
      }

      @Override
      public boolean detachIsDefault() {
        return false;
      }
    };

    final ExecutionConsole console = createFlexUnitRunnerConsole(project, env, processHandler);
    flexUnitConnection.addListener(new FlexUnitListener(processHandler));

    launchWithSelectedApplication(swfFilePath, params.getLauncherParameters());

    final RunContentBuilder contentBuilder =
      new RunContentBuilder(new DefaultExecutionResult(console, processHandler), env);
    Disposer.register(project, contentBuilder);
    return contentBuilder.showRunContent(contentToReuse);
  }

  @Override
  @Nullable
  protected RunContentDescriptor launchAirFlexUnit(final Project project,
                                                   final RunProfileState state,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final FlexUnitRunnerParameters params) throws ExecutionException {
    final ExecutionResult executionResult;
    final SwfPolicyFileConnection policyFileConnection = new SwfPolicyFileConnection();
    policyFileConnection.open(params.getSocketPolicyPort());

    final FlexUnitConnection flexUnitConnection = new FlexUnitConnection();
    flexUnitConnection.open(params.getPort());

    executionResult = state.execute(env.getExecutor(), this);
    if (executionResult == null) {
      flexUnitConnection.close();
      policyFileConnection.close();
      return null;
    }
    flexUnitConnection.addListener(new FlexUnitListener(executionResult.getProcessHandler()));
    executionResult.getProcessHandler().addProcessListener(new ProcessAdapter() {
      @Override
      public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
        flexUnitConnection.write("Finish");
      }

      @Override
      public void processTerminated(@NotNull ProcessEvent event) {
        flexUnitConnection.close();
        policyFileConnection.close();
      }
    });

    final RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, env);
    return contentBuilder.showRunContent(contentToReuse);
  }

  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof FlashRunConfiguration || profile instanceof FlexUnitRunConfiguration);
  }

  @Override
  @NotNull
  public String getRunnerId() {
    return "FlexRunner";
  }

  private static class FlexUnitListener implements FlexUnitConnection.Listener {
    private final ProcessHandler myProcessHandler;

    FlexUnitListener(ProcessHandler processHandler) {
      myProcessHandler = processHandler;
    }

    @Override
    public void statusChanged(FlexUnitConnection.ConnectionStatus status) {
      if (status == FlexUnitConnection.ConnectionStatus.CONNECTION_FAILED || status == FlexUnitConnection.ConnectionStatus.DISCONNECTED) {
        myProcessHandler.destroyProcess();
      }
    }

    @Override
    public void onData(final String line) {
      myProcessHandler.notifyTextAvailable(line + "\n", ProcessOutputTypes.STDOUT);
    }

    @Override
    public void onFinish() {
      // ignore
    }
  }
}
