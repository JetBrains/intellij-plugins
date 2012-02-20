package com.intellij.lang.javascript.flex.run;

import com.intellij.CommonBundle;
import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexStackTraceFilter;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.AirSigningOptions;
import com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import com.intellij.lang.javascript.flex.debug.FlexDebugProcess;
import com.intellij.lang.javascript.flex.debug.FlexDebugRunner;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitCommonParameters;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitConsoleProperties;
import com.intellij.lang.javascript.flex.flexunit.NewFlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.NewFlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.PathUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters.*;

/**
 * User: Maxim.Mossienko
 * Date: Mar 11, 2008
 * Time: 8:16:33 PM
 */
public abstract class FlexBaseRunner extends GenericProgramRunner {

  public static final RunProfileState EMPTY_RUN_STATE = new RunProfileState() {
    public ExecutionResult execute(final Executor executor, @NotNull final ProgramRunner runner) throws ExecutionException {
      return null;
    }

    public RunnerSettings getRunnerSettings() {
      return null;
    }

    public ConfigurationPerRunnerSettings getConfigurationSettings() {
      return null;
    }
  };

  @Nullable
  protected RunContentDescriptor doExecute(final Project project,
                                           final Executor executor,
                                           final RunProfileState state,
                                           final RunContentDescriptor contentToReuse,
                                           final ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final RunProfile runProfile = env.getRunProfile();

    final boolean isDebug = this instanceof FlexDebugRunner;
    try {
      if (runProfile instanceof RemoteFlashRunConfiguration) {
        final BCBasedRunnerParameters params = ((RemoteFlashRunConfiguration)runProfile).getRunnerParameters();
        final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC = params.checkAndGetModuleAndBC(project);
        return launchDebugProcess(moduleAndBC.first, moduleAndBC.second, params, executor, contentToReuse, env);
      }

      if (runProfile instanceof NewFlexUnitRunConfiguration) {
        final NewFlexUnitRunnerParameters params = ((NewFlexUnitRunConfiguration)runProfile).getRunnerParameters();
        final Pair<Module, FlexIdeBuildConfiguration> moduleAndConfig = params.checkAndGetModuleAndBC(project);
        final Module module = moduleAndConfig.first;
        final FlexIdeBuildConfiguration bc = moduleAndConfig.second;

        if (bc.getTargetPlatform() == TargetPlatform.Web) {
          final String outputFilePath = bc.getOutputFilePath(true);
          try {
            final String canonicalPath = new File(PathUtil.getParentPath(outputFilePath)).getCanonicalPath();
            FlashPlayerTrustUtil.updateTrustedStatus(module.getProject(), params.isTrusted(), false, canonicalPath);
          }
          catch (IOException e) {/**/}

          return launchWebFlexUnit(project, executor, contentToReuse, env, params, outputFilePath);
        }
        else {
          return launchAirFlexUnit(project, executor, state, contentToReuse, env, params);
        }
      }

      if (runProfile instanceof FlashRunConfiguration) {
        final FlashRunnerParameters params = ((FlashRunConfiguration)runProfile).getRunnerParameters();
        final Pair<Module, FlexIdeBuildConfiguration> moduleAndConfig = params.checkAndGetModuleAndBC(project);
        final Module module = moduleAndConfig.first;
        final FlexIdeBuildConfiguration bc = moduleAndConfig.second;

        if (bc.getTargetPlatform() == TargetPlatform.Web && !params.isLaunchUrl()) {
          try {
            final String canonicalPath = new File(PathUtil.getParentPath(bc.getOutputFilePath(true))).getCanonicalPath();
            FlashPlayerTrustUtil.updateTrustedStatus(module.getProject(), params.isRunTrusted(), isDebug, canonicalPath);
          }
          catch (IOException e) {/**/}
        }

        return launchFlexIdeConfig(module, bc, params, executor, state, contentToReuse, env);
      }
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }

    return null;
  }

  protected RunContentDescriptor launchDebugProcess(final Module module,
                                                    final FlexIdeBuildConfiguration bc,
                                                    final BCBasedRunnerParameters params,
                                                    final Executor executor,
                                                    final RunContentDescriptor contentToReuse,
                                                    final ExecutionEnvironment env) throws ExecutionException {
    final XDebugSession debugSession =
      XDebuggerManager.getInstance(module.getProject()).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
          try {
            if (params instanceof NewFlexUnitRunnerParameters) {
              return new FlexDebugProcess(session, bc, params) {
                @NotNull
                @Override
                public ExecutionConsole createConsole() {
                  try {
                    return createFlexUnitRunnerConsole(session.getProject(), env, getProcessHandler(), executor);
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
            throw new ExecutionException(e.getMessage(), e);
          }
        }
      });

    return debugSession.getRunContentDescriptor();
  }

  @Nullable
  protected abstract RunContentDescriptor launchAirFlexUnit(final Project project,
                                                            final Executor executor,
                                                            final RunProfileState state,
                                                            final RunContentDescriptor contentToReuse,
                                                            final ExecutionEnvironment env,
                                                            final FlexUnitCommonParameters params) throws ExecutionException;

  protected abstract RunContentDescriptor launchWebFlexUnit(final Project project,
                                                            final Executor executor,
                                                            final RunContentDescriptor contentToReuse,
                                                            final ExecutionEnvironment env,
                                                            final FlexUnitCommonParameters params,
                                                            final String swfFilePath) throws ExecutionException;

  @Nullable
  protected abstract RunContentDescriptor launchFlexIdeConfig(final Module module,
                                                              final FlexIdeBuildConfiguration config,
                                                              final FlashRunnerParameters params,
                                                              final Executor executor,
                                                              final RunProfileState state,
                                                              final RunContentDescriptor contentToReuse,
                                                              final ExecutionEnvironment environment) throws ExecutionException;

  public static ExecutionConsole createFlexUnitRunnerConsole(Project project,
                                                             ExecutionEnvironment env,
                                                             ProcessHandler processHandler,
                                                             Executor executor) throws ExecutionException {
    final RunProfile runConfiguration = env.getRunProfile();
    final FlexStackTraceFilter stackTraceFilter = new FlexStackTraceFilter(project);
    final FlexUnitConsoleProperties consoleProps = new FlexUnitConsoleProperties(((NewFlexUnitRunConfiguration)runConfiguration), executor);
    consoleProps.addStackTraceFilter(stackTraceFilter);

    final BaseTestsOutputConsoleView consoleView = SMTestRunnerConnectionUtil
      .createAndAttachConsole("FlexUnit", processHandler, consoleProps, env.getRunnerSettings(), env.getConfigurationSettings());
    consoleView.addMessageFilter(stackTraceFilter);
    Disposer.register(project, consoleView);
    return consoleView;
  }

  public static void launchWithSelectedApplication(final String urlOrPath, final LauncherParameters launcherParams) {
    switch (launcherParams.getLauncherType()) {
      case OSDefault:
        if (Desktop.isDesktopSupported()) {
          final Desktop desktop = Desktop.getDesktop();
          if (BrowserUtil.isAbsoluteURL(urlOrPath)) {
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
              try {
                desktop.browse(BrowserUtil.getURL(urlOrPath).toURI());
                break;
              }
              catch (IOException ignored) {/*ignored*/}
              catch (URISyntaxException ignored) {/*ignored*/}
            }
          }
          else {
            if (desktop.isSupported(Desktop.Action.OPEN)) {
              try {
                desktop.open(new File(urlOrPath));
                break;
              }
              catch (IOException ignored) {/*ignored*/}
              catch (IllegalArgumentException ignored) {/*ignored*/}
            }
          }
        }

        BrowserUtil.launchBrowser(urlOrPath);
        break;

      case Browser:
        final Runnable runnable1 = new Runnable() {
          public void run() {
            BrowsersConfiguration.launchBrowser(launcherParams.getBrowserFamily(),
                                                BrowserUtil.isAbsoluteURL(urlOrPath) ? urlOrPath : VfsUtil.pathToUrl(urlOrPath));
          }
        };

        final Application application1 = ApplicationManager.getApplication();
        if (application1.isDispatchThread()) {
          runnable1.run();
        }
        else {
          application1.invokeLater(runnable1);
        }
        break;

      case Player:
        final String playerPath = launcherParams.getPlayerPath();
        final String executablePath = SystemInfo.isMac && playerPath.endsWith(".app") ? FlexUtils.getMacExecutable(playerPath) : playerPath;
        try {
          if (executablePath == null) {
            throw new IOException("failed to find application to launch with");
          }

          Runtime.getRuntime().exec(new String[]{executablePath, urlOrPath});
        }
        catch (final IOException e) {
          final Runnable runnable2 = new Runnable() {
            public void run() {
              Messages.showErrorDialog(FlexBundle.message("cant.launch", urlOrPath, playerPath, e.getMessage()),
                                       CommonBundle.getErrorTitle());
            }
          };

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
                                                      final Sdk flexSdk,
                                                      final MobileAirPackageParameters packageParameters,
                                                      final String applicationId,
                                                      final boolean isDebug) {
    final Project project = module.getProject();
    final String apkPath = packageParameters.INSTALLER_FILE_LOCATION + "/" + packageParameters.INSTALLER_FILE_NAME;

    final String adtVersion;
    return (adtVersion = MobileAirUtil.getAdtVersion(project, flexSdk)) != null
           && MobileAirUtil.checkAdtVersion(module, flexSdk, adtVersion)
           && MobileAirUtil.checkAirRuntimeOnDevice(project, flexSdk, adtVersion)
           && MobileAirUtil.ensureCertificateExists(project, flexSdk)
           && MobileAirUtil.packageApk(project, packageParameters)
           && MobileAirUtil.installApk(project, flexSdk, apkPath, applicationId);
  }

  @Nullable
  public static String getApplicationId(final String airDescriptorPath) {
    final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(airDescriptorPath);
    if (descriptorFile != null) {
      try {
        return FlexUtils.findXMLElement(descriptorFile.getInputStream(), "<application><id>");
      }
      catch (IOException e) {/*ignore*/}
    }
    return null;
  }

  protected static MobileAirPackageParameters createAndroidPackageParams(final FlexIdeBuildConfiguration bc,
                                                                         final FlashRunnerParameters params,
                                                                         final boolean isDebug) {
    final List<FilePathAndPathInPackage> files = AirInstallerParametersBase.cloneList(bc.getAndroidPackagingOptions().getFilesToPackage());
    final String outputFilePath = bc.getOutputFilePath(true);
    files.add(0, new FilePathAndPathInPackage(outputFilePath, PathUtil.getFileName(outputFilePath)));

    final AndroidPackageType packageType = isDebug
                                           ? params.getDebugTransport() == FlashRunnerParameters.AirMobileDebugTransport.Network
                                             ? AndroidPackageType.DebugOverNetwork
                                             : AndroidPackageType.DebugOverUSB
                                           : AndroidPackageType.NoDebug;
    final AirSigningOptions signingOptions = bc.getAndroidPackagingOptions().getSigningOptions();
    final boolean temp = signingOptions.isUseTempCertificate();
    return new MobileAirPackageParameters(MobilePlatform.Android,
                                          packageType,
                                          IOSPackageType.DebugOverNetwork,
                                          true,
                                          bc.getSdk(),
                                          getAirDescriptorPath(bc, bc.getAndroidPackagingOptions()),
                                          bc.getAndroidPackagingOptions().getPackageFileName(),
                                          PathUtil.getParentPath(outputFilePath),
                                          files,
                                          MobileAirUtil.getLocalHostAddress(),
                                          params.getUsbDebugPort(),
                                          "",
                                          "",
                                          temp ? MobileAirUtil.getTempKeystorePath() : signingOptions.getKeystorePath(),
                                          temp ? MobileAirUtil.PKCS12_KEYSTORE_TYPE : signingOptions.getKeystoreType(),
                                          temp ? MobileAirUtil.TEMP_KEYSTORE_PASSWORD : signingOptions.getKeystorePassword(),
                                          temp ? "" : signingOptions.getKeyAlias(),
                                          temp ? "" : signingOptions.getKeyPassword(),
                                          temp ? "" : signingOptions.getProvider(),
                                          temp ? "" : signingOptions.getTsa());
  }

  public static void launchOnAndroidDevice(final Project project, final Sdk flexSdk, final String applicationId, final boolean isDebug) {
    if (MobileAirUtil.launchAndroidApplication(project, flexSdk, applicationId)) {
      ToolWindowManager.getInstance(project).notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.INFO,
                                                             FlexBundle.message("android.application.launched"));
    }
  }

  public static GeneralCommandLine createAdlCommandLine(final BCBasedRunnerParameters params,
                                                        final FlexIdeBuildConfiguration bc,
                                                        final @Nullable String airRuntimePath) throws CantRunException {
    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      throw new CantRunException(FlexBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), params.getModuleName()));
    }

    final GeneralCommandLine commandLine = new GeneralCommandLine();

    commandLine.setExePath(FileUtil.toSystemDependentName(FlexSdkUtils.getAdlPath(sdk)));

    if (airRuntimePath != null) {
      commandLine.addParameter("-runtime");
      commandLine.addParameter(airRuntimePath);
    }

    if (bc.getNature().isDesktopPlatform()) {
      final String adlOptions =
        params instanceof FlashRunnerParameters ? ((FlashRunnerParameters)params).getAdlOptions() : "";
      if (!StringUtil.isEmptyOrSpaces(adlOptions)) {
        commandLine.addParameters(StringUtil.split(adlOptions, " "));
      }

      final AirDesktopPackagingOptions packagingOptions = bc.getAirDesktopPackagingOptions();
      commandLine.addParameter(FileUtil.toSystemDependentName(getAirDescriptorPath(bc, packagingOptions)));
      commandLine.addParameter(FileUtil.toSystemDependentName(PathUtil.getParentPath(bc.getOutputFilePath(true))));
      final String programParameters =
        params instanceof FlashRunnerParameters ? ((FlashRunnerParameters)params).getAirProgramParameters() : "";
      if (!StringUtil.isEmptyOrSpaces(programParameters)) {
        commandLine.addParameter("--");
        commandLine.addParameters(StringUtil.split(programParameters, " "));
      }
    }
    else {
      assert params instanceof FlashRunnerParameters;
      final FlashRunnerParameters flashParams = (FlashRunnerParameters)params;
      assert bc.getNature().isMobilePlatform() : bc.getTargetPlatform();
      assert flashParams.getMobileRunTarget() == FlashRunnerParameters.AirMobileRunTarget.Emulator : flashParams.getMobileRunTarget();

      commandLine.addParameter("-profile");
      commandLine.addParameter("mobileDevice");

      commandLine.addParameter("-screensize");
      final String adlAlias = flashParams.getEmulator().adlAlias;
      if (adlAlias != null) {
        commandLine.addParameter(adlAlias);
      }
      else {
        commandLine.addParameter(flashParams.getScreenWidth() + "x" + flashParams.getScreenHeight() +
                                 ":" + flashParams.getFullScreenWidth() + "x" + flashParams.getFullScreenHeight());
      }

      final String adlOptions = flashParams.getEmulatorAdlOptions();
      if (!StringUtil.isEmptyOrSpaces(adlOptions)) {
        commandLine.addParameters(StringUtil.split(adlOptions, " "));
      }

      // todo why android? which to take for emulator should probably be selected in run configuration.
      final AndroidPackagingOptions packagingOptions = bc.getAndroidPackagingOptions();
      commandLine.addParameter(FileUtil.toSystemDependentName(getAirDescriptorPath(bc, packagingOptions)));
      commandLine.addParameter(FileUtil.toSystemDependentName(PathUtil.getParentPath(bc.getOutputFilePath(true))));
    }

    return commandLine;
  }

  public static String getAirDescriptorPath(final FlexIdeBuildConfiguration bc, final AirPackagingOptions packagingOptions) {
    return PathUtil.getParentPath(bc.getOutputFilePath(true)) + "/" + getAirDescriptorFileName(bc, packagingOptions);
  }

  public static String getAirDescriptorFileName(final FlexIdeBuildConfiguration config, final AirPackagingOptions packagingOptions) {
    return packagingOptions.isUseGeneratedDescriptor() ? BCUtils.getGeneratedAirDescriptorName(config, packagingOptions)
                                                       : PathUtil.getFileName(packagingOptions.getCustomDescriptorPath());
  }
}
