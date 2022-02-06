// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.run;

import com.intellij.CommonBundle;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.compiler.options.CompileStepBeforeRunNoErrorCheck;
import com.intellij.execution.*;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.RunDialog;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.util.ExecUtil;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexStackTraceFilter;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageUtil;
import com.intellij.lang.javascript.flex.actions.airpackage.DeviceInfo;
import com.intellij.lang.javascript.flex.build.FlexCompilationUtils;
import com.intellij.lang.javascript.flex.debug.FlexDebugProcess;
import com.intellij.lang.javascript.flex.debug.FlexDebugRunner;
import com.intellij.lang.javascript.flex.flexunit.FlexQualifiedNameLocationProvider;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.AirDesktopPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.notification.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.navigation.Place;
import com.intellij.util.PathUtil;
import com.intellij.xdebugger.*;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AppDescriptorForEmulator;
import static com.intellij.lang.javascript.flex.run.RemoteFlashRunnerParameters.RemoteDebugTarget;

public abstract class FlexBaseRunner extends GenericProgramRunner {
  public static final NotificationGroup COMPILE_BEFORE_LAUNCH_NOTIFICATION_GROUP = NotificationGroup
    .toolWindowGroup("Flash app not compiled before launch", ToolWindowId.RUN, false,
                     FlexBundle.message("check.flash.app.compiled.before.launch.notification.group"));

  @Override
  @Nullable
  protected RunContentDescriptor doExecute(@NotNull final RunProfileState state,
                                           @NotNull final ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final RunProfile runProfile = env.getRunProfile();

    final boolean isDebug = this instanceof FlexDebugRunner;
    try {
      if (runProfile instanceof RunProfileWithCompileBeforeLaunchOption) {
        checkMakeBeforeRunEnabled(env.getProject(), runProfile);
      }

      if (runProfile instanceof RemoteFlashRunConfiguration) {
        final RemoteFlashRunnerParameters params = ((RemoteFlashRunConfiguration)runProfile).getRunnerParameters();
        final Pair<Module, FlexBuildConfiguration> moduleAndBC = params.checkAndGetModuleAndBC(env.getProject());

        if (params.getDebugTransport() == FlashRunnerParameters.AirMobileDebugTransport.USB) {
          final Sdk sdk = moduleAndBC.second.getSdk();
          assert sdk != null;

          if (params.getRemoteDebugTarget() == RemoteDebugTarget.AndroidDevice) {
            if (!AirPackageUtil.startAdbServer(env.getProject(), sdk) ||
                !AirPackageUtil.scanAndroidDevices(env.getProject(), sdk, params) ||
                !AirPackageUtil.androidForwardTcpPort(env.getProject(), sdk, params.getDeviceInfo(), params.getUsbDebugPort())) {
              return null;
            }
          }
          else if (params.getRemoteDebugTarget() == RemoteDebugTarget.iOSDevice) {
            final String adtVersion = AirPackageUtil.getAdtVersion(env.getProject(), sdk);

            if (!AirPackageUtil.checkAdtVersionForPackaging(env.getProject(), adtVersion, "3.4", sdk.getName(),
                                                            FlexBundle.message("air.ios.debug.via.usb.requires.3.4"))) {
              return null;
            }
            if (!AirPackageUtil.scanIosDevices(env.getProject(), sdk, params)) {
              return null;
            }

            final DeviceInfo device = params.getDeviceInfo();
            final int deviceHandle = device == null ? -1 : device.IOS_HANDLE;
            if (deviceHandle < 0) {
              return null;
            }

            if (!AirPackageUtil.iosForwardTcpPort(env.getProject(), sdk, params.getUsbDebugPort(), deviceHandle)) {
              return null;
            }
          }
        }

        return launchDebugProcess(moduleAndBC.first, moduleAndBC.second, params, env);
      }

      if (runProfile instanceof FlexUnitRunConfiguration) {
        final FlexUnitRunnerParameters params = ((FlexUnitRunConfiguration)runProfile).getRunnerParameters();
        final Pair<Module, FlexBuildConfiguration> moduleAndConfig = params.checkAndGetModuleAndBC(env.getProject());
        final Module module = moduleAndConfig.first;
        final FlexBuildConfiguration bc = moduleAndConfig.second;

        if (bc.getTargetPlatform() == TargetPlatform.Web) {
          FlashPlayerTrustUtil.updateTrustedStatus(module, bc, isDebug, params.isTrusted());
          return launchWebFlexUnit(env.getProject(), env.getContentToReuse(), env, params, bc.getActualOutputFilePath());
        }
        else {
          return launchAirFlexUnit(env.getProject(), state, env.getContentToReuse(), env, params);
        }
      }

      if (runProfile instanceof FlashRunConfiguration) {
        final FlashRunnerParameters params = ((FlashRunConfiguration)runProfile).getRunnerParameters();
        params.setDeviceInfo(null);
        final Pair<Module, FlexBuildConfiguration> moduleAndConfig = params.checkAndGetModuleAndBC(env.getProject());
        final Module module = moduleAndConfig.first;
        final FlexBuildConfiguration bc = moduleAndConfig.second;
        if (bc.isSkipCompile()) {
          showBCCompilationSkippedWarning(module, bc);
        }

        if (isDebug && SystemInfo.isMac && bc.getTargetPlatform() == TargetPlatform.Web) {
          checkDebuggerFromSdk4(env.getProject(), runProfile, params, bc);
        }

        if (bc.getTargetPlatform() == TargetPlatform.Web && !params.isLaunchUrl()) {
          FlashPlayerTrustUtil.updateTrustedStatus(module, bc, isDebug, params.isRunTrusted());
        }

        return launchFlexConfig(module, bc, params, state, env.getContentToReuse(), env);
      }
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }

    return null;
  }

  protected RunContentDescriptor launchDebugProcess(final Module module,
                                                    final FlexBuildConfiguration bc,
                                                    final BCBasedRunnerParameters params,
                                                    final ExecutionEnvironment env) throws ExecutionException {
    final XDebugSession debugSession =
      XDebuggerManager.getInstance(module.getProject()).startSession(env, new XDebugProcessStarter() {
        @Override
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
          try {
            if (params instanceof FlexUnitRunnerParameters) {
              return new FlexDebugProcess(session, bc, params) {
                @NotNull
                @Override
                public ExecutionConsole createConsole() {
                  try {
                    return createFlexUnitRunnerConsole(session.getProject(), env, getProcessHandler());
                  }
                  catch (ExecutionException e) {
                    Logger.getInstance(FlexBaseRunner.class.getName()).error(e);
                  }
                  return super.createConsole();
                }
              };
            }
            return new FlexDebugProcess(session, bc, params);
          }
          catch (IOException e) {
            iosStopForwardTcpPortIfNeeded(bc, params);
            throw new ExecutionException(e.getMessage(), e);
          }
        }
      });

    debugSession.addSessionListener(new XDebugSessionListener() {
      @Override
      public void sessionStopped() {
        iosStopForwardTcpPortIfNeeded(bc, params);
      }
    });

    return debugSession.getRunContentDescriptor();
  }

  private static void iosStopForwardTcpPortIfNeeded(final FlexBuildConfiguration bc, final BCBasedRunnerParameters params) {
    if (params instanceof RemoteFlashRunnerParameters &&
        ((RemoteFlashRunnerParameters)params).getRemoteDebugTarget() == RemoteDebugTarget.iOSDevice &&
        ((RemoteFlashRunnerParameters)params).getDebugTransport() == FlashRunnerParameters.AirMobileDebugTransport.USB) {
      AirPackageUtil.iosStopForwardTcpPort(bc.getSdk(), ((RemoteFlashRunnerParameters)params).getUsbDebugPort());
    }
    else if (bc.getTargetPlatform() == TargetPlatform.Mobile &&
             params instanceof FlashRunnerParameters &&
             ((FlashRunnerParameters)params).getMobileRunTarget() == FlashRunnerParameters.AirMobileRunTarget.iOSDevice &&
             ((FlashRunnerParameters)params).getDebugTransport() == FlashRunnerParameters.AirMobileDebugTransport.USB) {
      AirPackageUtil.iosStopForwardTcpPort(bc.getSdk(), ((FlashRunnerParameters)params).getUsbDebugPort());
    }
  }

  @Nullable
  protected abstract RunContentDescriptor launchAirFlexUnit(final Project project,
                                                            final RunProfileState state,
                                                            final RunContentDescriptor contentToReuse,
                                                            final ExecutionEnvironment env,
                                                            final FlexUnitRunnerParameters params) throws ExecutionException;

  protected abstract RunContentDescriptor launchWebFlexUnit(final Project project,
                                                            final RunContentDescriptor contentToReuse,
                                                            final ExecutionEnvironment env,
                                                            final FlexUnitRunnerParameters params,
                                                            final String swfFilePath) throws ExecutionException;

  @Nullable
  protected abstract RunContentDescriptor launchFlexConfig(final Module module,
                                                           final FlexBuildConfiguration config,
                                                           final FlashRunnerParameters params,
                                                           final RunProfileState state,
                                                           final RunContentDescriptor contentToReuse,
                                                           final ExecutionEnvironment environment) throws ExecutionException;

  public static ExecutionConsole createFlexUnitRunnerConsole(Project project,
                                                             ExecutionEnvironment env,
                                                             ProcessHandler processHandler) throws ExecutionException {
    FlexStackTraceFilter stackTraceFilter = new FlexStackTraceFilter(project);

    SMTRunnerConsoleProperties consoleProps = new FlexUnitConsoleProperties((FlexUnitRunConfiguration)env.getRunProfile(), env);
    consoleProps.addStackTraceFilter(stackTraceFilter);

    BaseTestsOutputConsoleView consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole("FlexUnit", processHandler, consoleProps);
    consoleView.addMessageFilter(stackTraceFilter);
    Disposer.register(project, consoleView);
    return consoleView;
  }

  public static void launchWithSelectedApplication(final String urlOrPath, final LauncherParameters launcherParams) {
    switch (launcherParams.getLauncherType()) {
      case OSDefault:
        BrowserUtil.open(urlOrPath);
        break;

      case Browser:
        final Runnable runnable1 =
          () -> BrowserLauncher.getInstance().browse(BrowserUtil.isAbsoluteURL(urlOrPath) ? urlOrPath : VfsUtilCore.pathToUrl(urlOrPath),
                                                     launcherParams.getBrowser());

        final Application application1 = ApplicationManager.getApplication();
        if (application1.isDispatchThread()) {
          runnable1.run();
        }
        else {
          application1.invokeLater(runnable1);
        }
        break;

      case Player:
        try {
          if (SystemInfo.isMac) {
            if (launcherParams.isNewPlayerInstance()) {
              Runtime.getRuntime().exec(new String[]{ExecUtil.getOpenCommandPath(), "-n", "-a", launcherParams.getPlayerPath(), urlOrPath});
            }
            else {
              Runtime.getRuntime().exec(new String[]{ExecUtil.getOpenCommandPath(), "-a", launcherParams.getPlayerPath(), urlOrPath});
            }
          }
          else {
            Runtime.getRuntime().exec(new String[]{launcherParams.getPlayerPath(), urlOrPath});
          }
          // todo read error stream, report errors
          // todo keep process to be able to kill it on user demand
        }
        catch (final IOException e) {
          final Runnable runnable2 =
            () -> Messages.showErrorDialog(FlexBundle.message("cant.launch", urlOrPath, launcherParams.getPlayerPath(), e.getMessage()),
                                           CommonBundle.getErrorTitle());

          final Application application2 = ApplicationManager.getApplication();
          if (application2.isDispatchThread()) {
            runnable2.run();
          }
          else {
            application2.invokeLater(runnable2);
          }
        }
        break;
    }
  }

  public static boolean packAndInstallToAndroidDevice(final Module module,
                                                      final FlexBuildConfiguration bc,
                                                      final FlashRunnerParameters runnerParameters,
                                                      final String applicationId,
                                                      final boolean isDebug) {
    final Project project = module.getProject();
    final Sdk sdk = bc.getSdk();
    final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
    final String apkPath = outputFolder + "/" + bc.getAndroidPackagingOptions().getPackageFileName() + ".apk";

    final String adtVersion;
    return (adtVersion = AirPackageUtil.getAdtVersion(project, sdk)) != null
           && AirPackageUtil.startAdbServer(project, sdk)
           && AirPackageUtil.scanAndroidDevices(project, sdk, runnerParameters)
           && AirPackageUtil.checkAirRuntimeOnDevice(project, sdk, runnerParameters, adtVersion)
           && AirPackageUtil.packageApk(module, bc, runnerParameters, isDebug)
           && (!runnerParameters.isClearAppDataOnEachLaunch() ||
               AirPackageUtil.uninstallAndroidApplication(project, sdk, runnerParameters.getDeviceInfo(), applicationId))
           && AirPackageUtil.installApk(project, sdk, runnerParameters.getDeviceInfo(), apkPath,
                                        runnerParameters.isClearAppDataOnEachLaunch());
  }

  public static boolean packAndInstallToIOSSimulator(final Module module,
                                                     final FlexBuildConfiguration bc,
                                                     final FlashRunnerParameters runnerParameters,
                                                     final String adtVersion,
                                                     final String applicationId,
                                                     final boolean isDebug) {
    final Sdk sdk = bc.getSdk();
    assert sdk != null;
    final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
    final String ipaPath = outputFolder + "/" + bc.getIosPackagingOptions().getPackageFileName() + ".ipa";

    if (!AirPackageUtil.checkAdtVersionForPackaging(module.getProject(), adtVersion, "3.3", sdk.getName(),
                                                    FlexBundle.message("air.ios.simulator.requires.3.3"))) {
      return false;
    }

    return (AirPackageUtil.packageIpaForSimulator(module, bc, runnerParameters, isDebug) &&
            AirPackageUtil.installOnIosSimulator(module.getProject(), sdk, ipaPath, applicationId,
                                                 runnerParameters.getIOSSimulatorSdkPath(), runnerParameters.getIOSSimulatorDevice()));
  }

  public static boolean packAndInstallToIOSDevice(final Module module,
                                                  final FlexBuildConfiguration bc,
                                                  final FlashRunnerParameters runnerParameters,
                                                  final String adtVersion,
                                                  final boolean isDebug) {
    final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
    final String ipaPath = outputFolder + "/" + bc.getIosPackagingOptions().getPackageFileName() + ".ipa";

    return AirPackageUtil.packageIpaForDevice(module, bc, runnerParameters, adtVersion, isDebug) &&
           AirPackageUtil.scanIosDevices(module.getProject(), bc.getSdk(), runnerParameters) &&
           AirPackageUtil.installOnIosDevice(module.getProject(), bc.getSdk(), runnerParameters, ipaPath);
  }

  @Nullable
  public static String getApplicationId(final String airDescriptorPath) {
    final VirtualFile descriptorFile = WriteAction.compute(() -> {
      final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(airDescriptorPath);
      if (file != null) {
        file.refresh(false, false);
      }
      return file;
    });

    if (descriptorFile != null) {
      try {
        return FlexUtils.findXMLElement(descriptorFile.getInputStream(), "<application><id>");
      }
      catch (IOException ignored) {/*ignore*/}
    }
    return null;
  }

  @Nullable
  public static String getApplicationName(final String airDescriptorPath) {
    final VirtualFile descriptorFile = WriteAction.compute(() -> {
      final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(airDescriptorPath);
      if (file != null) {
        file.refresh(false, false);
      }
      return file;
    });

    if (descriptorFile != null) {
      try {
        return FlexUtils.findXMLElement(descriptorFile.getInputStream(), "<application><name>");
      }
      catch (IOException ignored) {/*ignore*/}
    }
    return null;
  }

  public static void launchOnAndroidDevice(final Project project,
                                           final Sdk flexSdk,
                                           final @Nullable DeviceInfo device,
                                           final String applicationId,
                                           final boolean isDebug) {
    if (AirPackageUtil.launchAndroidApplication(project, flexSdk, device, applicationId)) {
      ToolWindowManager.getInstance(project).notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.INFO,
                                                             FlexBundle.message("android.application.launched"));
    }
  }

  public static void launchOnIosSimulator(final Project project,
                                          final Sdk flexSdk,
                                          final String applicationId,
                                          final String iOSSdkPath,
                                          final String simulatorDevice,
                                          final boolean isDebug) {
    if (AirPackageUtil.launchOnIosSimulator(project, flexSdk, applicationId, iOSSdkPath, simulatorDevice)) {
      ToolWindowManager.getInstance(project).notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.INFO,
                                                             FlexBundle.message("ios.simulator.application.launched"));
    }
  }

  public static GeneralCommandLine createAdlCommandLine(final Project project,
                                                        final BCBasedRunnerParameters params,
                                                        final FlexBuildConfiguration bc,
                                                        @Nullable String airRuntimePath) throws CantRunException {
    assert params instanceof FlashRunnerParameters || params instanceof FlexUnitRunnerParameters : params;
    assert bc.getTargetPlatform() == TargetPlatform.Desktop || bc.getTargetPlatform() == TargetPlatform.Mobile;

    final Module module = ModuleManager.getInstance(project).findModuleByName(params.getModuleName());
    final Sdk sdk = bc.getSdk();

    if (module == null) {
      throw new CantRunException(FlexBundle.message("module.not.found", params.getModuleName()));
    }
    if (sdk == null) {
      throw new CantRunException(FlexCommonBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), params.getModuleName()));
    }

    final GeneralCommandLine commandLine = new GeneralCommandLine();

    commandLine.setExePath(FileUtil.toSystemDependentName(FlexSdkUtils.getAdlPath(sdk)));

    String adlOptions;
    if (params instanceof FlashRunnerParameters) {
      adlOptions = bc.getTargetPlatform() == TargetPlatform.Desktop ? ((FlashRunnerParameters)params).getAdlOptions()
                                                                    : ((FlashRunnerParameters)params).getEmulatorAdlOptions();
    }
    else {
      adlOptions = bc.getTargetPlatform() == TargetPlatform.Desktop ? ""
                                                                    : ((FlexUnitRunnerParameters)params).getEmulatorAdlOptions();
    }

    final List<String> runtimePath = FlexCommonUtils.getOptionValues(adlOptions, "runtime");
    if (!runtimePath.isEmpty()) {
      adlOptions = FlexCommonUtils.removeOptions(adlOptions, "runtime");
      airRuntimePath = runtimePath.get(0);
    }

    if (airRuntimePath != null) {
      commandLine.addParameter("-runtime");
      commandLine.addParameter(airRuntimePath);
    }

    final Collection<VirtualFile> aneFiles = FlexCompilationUtils.getANEFiles(ModuleRootManager.getInstance(module), bc.getDependencies());
    if (!aneFiles.isEmpty()) {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) indicator.setIndeterminate(true);
        FlexCompilationUtils.unzipANEFiles(aneFiles, indicator);
      }, "Unzipping ANE files", true, project);
    }

    if (bc.getNature().isDesktopPlatform()) {
      final AirDesktopPackagingOptions packagingOptions = bc.getAirDesktopPackagingOptions();
      final String descriptorPath = getAirDescriptorPath(bc, packagingOptions);

      if ((FlexSdkUtils.isAirSdkWithoutFlex(sdk) || StringUtil.compareVersionNumbers(sdk.getVersionString(), "4.1") >= 0) &&
          FlexCommonUtils.getOptionValues(adlOptions, "profile").isEmpty()) {

        final String profiles = getSupportedProfiles(descriptorPath);
        if (profiles == null || profiles.contains("extendedDesktop")) {
          commandLine.addParameter("-profile");
          commandLine.addParameter("extendedDesktop");
        }
      }

      if (!StringUtil.isEmptyOrSpaces(adlOptions)) {
        for (StringTokenizer tokenizer = new CommandLineTokenizer(adlOptions); tokenizer.hasMoreTokens(); ) {
          commandLine.addParameter(tokenizer.nextToken());
        }
      }

      if (!aneFiles.isEmpty()) {
        commandLine.addParameter("-extdir");
        commandLine.addParameter(FlexCompilationUtils.getPathToUnzipANE());
      }

      commandLine.addParameter(FileUtil.toSystemDependentName(descriptorPath));
      commandLine.addParameter(FileUtil.toSystemDependentName(PathUtil.getParentPath(bc.getActualOutputFilePath())));
      final String programParameters =
        params instanceof FlashRunnerParameters ? ((FlashRunnerParameters)params).getAirProgramParameters() : "";
      if (!StringUtil.isEmptyOrSpaces(programParameters)) {
        commandLine.addParameter("--");
        for (StringTokenizer tokenizer = new CommandLineTokenizer(programParameters); tokenizer.hasMoreTokens(); ) {
          commandLine.addParameter(tokenizer.nextToken());
        }
      }
    }
    else {
      final AppDescriptorForEmulator descriptorForEmulator = params instanceof FlashRunnerParameters
                                                             ? ((FlashRunnerParameters)params).getAppDescriptorForEmulator()
                                                             : ((FlexUnitRunnerParameters)params).getAppDescriptorForEmulator();
      final String descriptorPath = getDescriptorForEmulatorPath(bc, descriptorForEmulator);

      if (params instanceof FlashRunnerParameters) {
        final FlashRunnerParameters.AirMobileRunTarget mobileRunTarget = ((FlashRunnerParameters)params).getMobileRunTarget();
        assert mobileRunTarget == FlashRunnerParameters.AirMobileRunTarget.Emulator : mobileRunTarget;
      }

      if (FlexCommonUtils.getOptionValues(adlOptions, "profile").isEmpty()) {
        final String profiles = getSupportedProfiles(descriptorPath);
        if (profiles == null || profiles.contains("extendedMobileDevice")) {
          commandLine.addParameter("-profile");
          commandLine.addParameter("extendedMobileDevice");
        }
      }

      final FlashRunnerParameters.Emulator emulator = params instanceof FlashRunnerParameters
                                                      ? ((FlashRunnerParameters)params).getEmulator()
                                                      : FlashRunnerParameters.Emulator.NexusOne;
      final boolean customSize = emulator.adlAlias == null;

      commandLine.addParameter("-screensize");
      if (customSize) {
        assert params instanceof FlashRunnerParameters;
        final FlashRunnerParameters flashParams = (FlashRunnerParameters)params;
        commandLine.addParameter(flashParams.getScreenWidth() + "x" + flashParams.getScreenHeight() +
                                 ":" + flashParams.getFullScreenWidth() + "x" + flashParams.getFullScreenHeight());
      }
      else {
        commandLine.addParameter(emulator.adlAlias);
      }

      if (FlexCommonUtils.getOptionValues(adlOptions, "XscreenDPI").isEmpty()) {
        if (customSize && ((FlashRunnerParameters)params).getScreenDpi() > 0) {
          commandLine.addParameter("-XscreenDPI");
          commandLine.addParameter(String.valueOf(((FlashRunnerParameters)params).getScreenDpi()));
        }
        else if (!customSize && emulator.screenDPI > 0) {
          commandLine.addParameter("-XscreenDPI");
          commandLine.addParameter(String.valueOf(emulator.screenDPI));
        }
      }

      if (FlexCommonUtils.getOptionValues(adlOptions, "XversionPlatform").isEmpty() && emulator.versionPlatform != null) {
        commandLine.addParameter("-XversionPlatform");
        commandLine.addParameter(emulator.versionPlatform);
      }

      if (!StringUtil.isEmptyOrSpaces(adlOptions)) {
        for (StringTokenizer tokenizer = new CommandLineTokenizer(adlOptions); tokenizer.hasMoreTokens(); ) {
          commandLine.addParameter(tokenizer.nextToken());
        }
      }

      if (!aneFiles.isEmpty()) {
        commandLine.addParameter("-extdir");
        commandLine.addParameter(FlexCompilationUtils.getPathToUnzipANE());
      }

      commandLine.addParameter(FileUtil.toSystemDependentName(descriptorPath));
      commandLine.addParameter(FileUtil.toSystemDependentName(PathUtil.getParentPath(bc.getActualOutputFilePath())));
    }

    return commandLine;
  }

  @Nullable
  private static String getSupportedProfiles(final String descriptorPath) {
    final File descriptorFile = new File(descriptorPath);
    if (descriptorFile.isFile()) {
      try {
        return JDOMUtil.load(descriptorFile).getChildTextNormalize("supportedProfiles", JDOMUtil.load(descriptorFile).getNamespace());
      }
      catch (JDOMException | IOException ignore) {/*ignore*/}
    }

    return null;
  }

  private static String getDescriptorForEmulatorPath(final FlexBuildConfiguration bc,
                                                     final AppDescriptorForEmulator appDescriptorForEmulator) {
    final String airDescriptorPath;
    switch (appDescriptorForEmulator) {
      case Android:
        airDescriptorPath = getAirDescriptorPath(bc, bc.getAndroidPackagingOptions());
        break;
      case IOS:
        airDescriptorPath = getAirDescriptorPath(bc, bc.getIosPackagingOptions());
        break;
      default:
        assert false;
        airDescriptorPath = "";
    }
    return airDescriptorPath;
  }

  public static String getAirDescriptorPath(final FlexBuildConfiguration bc, final AirPackagingOptions packagingOptions) {
    return PathUtil.getParentPath(bc.getActualOutputFilePath()) + "/" + getAirDescriptorFileName(bc, packagingOptions);
  }

  private static String getAirDescriptorFileName(final FlexBuildConfiguration bc, final AirPackagingOptions packagingOptions) {
    return packagingOptions.isUseGeneratedDescriptor() || bc.isTempBCForCompilation()
           ? BCUtils.getGeneratedAirDescriptorName(bc, packagingOptions)
           : PathUtil.getFileName(packagingOptions.getCustomDescriptorPath());
  }

  private static void checkMakeBeforeRunEnabled(final Project project, final RunProfile runProfile) {
    int count = RunManagerEx.getTasksCount(project, (RunConfiguration)runProfile, CompileStepBeforeRun.ID, CompileStepBeforeRunNoErrorCheck.ID);
    if (count == 0) {
      for (RunnerAndConfigurationSettings settings : RunManager.getInstance(project).getConfigurationSettingsList(((RunConfiguration)runProfile).getType())) {
        if (settings.getConfiguration() == runProfile) {
          showMakeBeforeRunTurnedOffWarning(project, settings);
          break;
        }
      }
    }
  }

  private static void showMakeBeforeRunTurnedOffWarning(final Project project, final RunnerAndConfigurationSettings configuration) {
    final String message = FlexBundle.message("run.when.compile.before.run.turned.off");
    COMPILE_BEFORE_LAUNCH_NOTIFICATION_GROUP.createNotification(message, NotificationType.WARNING).setListener(new NotificationListener() {
      @Override
      public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          notification.expire();

          if ("RunConfiguration".equals(event.getDescription())) {
            RunDialog.editConfiguration(project, configuration, FlexBundle.message("edit.configuration.title"));
          }
          else if ("DisableWarning".equals(event.getDescription())) {
            disableCompilationSkippedWarning(project);
          }
        }
      }
    }).notify(project);
  }

  private static void showBCCompilationSkippedWarning(final Module module, final FlexBuildConfiguration bc) {
    final String message = FlexBundle.message("run.when.ide.builder.turned.off", bc.getName(), module.getName());
    COMPILE_BEFORE_LAUNCH_NOTIFICATION_GROUP.createNotification(message, NotificationType.WARNING).setListener(new NotificationListener() {
      @Override
      public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          notification.expire();

          if ("BuildConfiguration".equals(event.getDescription())) {
            final ProjectStructureConfigurable projectStructureConfigurable = ProjectStructureConfigurable.getInstance(module.getProject());
            ShowSettingsUtil.getInstance().editConfigurable(module.getProject(), projectStructureConfigurable, () -> {
              Place p = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getPlaceFor(module, bc.getName());
              projectStructureConfigurable.navigateTo(p, true);
            });
          }
          else if ("DisableWarning".equals(event.getDescription())) {
            disableCompilationSkippedWarning(module.getProject());
          }
        }
      }
    }).notify(module.getProject());
  }

  private static void disableCompilationSkippedWarning(final Project project) {
    NotificationsConfiguration.getNotificationsConfiguration()
      .changeSettings(COMPILE_BEFORE_LAUNCH_NOTIFICATION_GROUP.getDisplayId(), NotificationDisplayType.NONE, false, false);
    ToolWindowManager.getInstance(project)
      .notifyByBalloon(EventLog.LOG_TOOL_WINDOW_ID, MessageType.INFO, FlexBundle.message("make.before.launch.warning.disabled"));
  }

  private static void checkDebuggerFromSdk4(final Project project,
                                            final RunProfile runProfile,
                                            final FlashRunnerParameters params,
                                            final FlexBuildConfiguration bc) {
    final Sdk sdk = bc.getSdk();
    assert sdk != null;

    final Sdk sdkForDebugger = FlexDebugProcess.getDebuggerSdk(params.getDebuggerSdkRaw(), sdk);
    if (!FlexSdkUtils.isAirSdkWithoutFlex(sdk) && StringUtil.compareVersionNumbers(sdkForDebugger.getVersionString(), "4") < 0) {
      final HyperlinkListener listener = new HyperlinkAdapter() {
        @Override
        protected void hyperlinkActivated(final HyperlinkEvent e) {
          if ("RunConfiguration".equals(e.getDescription())) {
            for (RunnerAndConfigurationSettings configuration : RunManager.getInstance(project).getConfigurationSettingsList(((RunConfiguration)runProfile).getType())) {
              if (configuration.getConfiguration() == runProfile) {
                RunDialog.editConfiguration(project, configuration, FlexBundle.message("edit.configuration.title"));
                break;
              }
            }
          }
        }
      };
      final String message = FlexBundle.message("flex.sdk.3.mac.debug.problem", sdkForDebugger.getVersionString());
      ToolWindowManager.getInstance(project).notifyByBalloon(ToolWindowId.DEBUG, MessageType.WARNING, message, null, listener);
    }
  }

  private static class FlexUnitConsoleProperties extends SMTRunnerConsoleProperties {
    FlexUnitConsoleProperties(RunConfiguration runConfiguration, ExecutionEnvironment env) {
      super(runConfiguration, "FlexUnit", env.getExecutor());
    }

    @Nullable
    @Override
    public SMTestLocator getTestLocator() {
      return FlexQualifiedNameLocationProvider.INSTANCE;
    }
  }
}
