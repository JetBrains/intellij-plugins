package com.intellij.lang.javascript.flex.run;

import com.intellij.CommonBundle;
import com.intellij.compiler.CompilerSettingsFactory;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.*;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.impl.RunDialog;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.facet.FacetManager;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectSettingsFactory;
import com.intellij.lang.javascript.flex.debug.FlexDebugProcess;
import com.intellij.lang.javascript.flex.debug.FlexDebugRunner;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitConsoleProperties;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters.*;
import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.*;

/**
 * User: Maxim.Mossienko
 * Date: Mar 11, 2008
 * Time: 8:16:33 PM
 */
public abstract class FlexBaseRunner extends GenericProgramRunner {

  private static final int URL_CHECK_TIMEOUT = 3000;

  protected RunContentDescriptor doExecute(final Project project,
                                           final Executor executor,
                                           final RunProfileState state,
                                           final RunContentDescriptor contentToReuse,
                                           final ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final RunProfile runProfile = env.getRunProfile();
    final FlexRunnerParameters flexRunnerParameters = ((FlexRunConfiguration)runProfile).getRunnerParameters();

    final Module module = ModuleManager.getInstance(project).findModuleByName(flexRunnerParameters.getModuleName());
    final Sdk flexSdk = module == null ? null : FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
    if (flexSdk == null) {
      throw new CantRunException(FlexBundle.message("cannot.find.flex.sdk"));
    }

    final boolean isDebug = this instanceof FlexDebugRunner;
    final boolean ok = isRunAsAir(flexRunnerParameters)
                       ? checkAirParams((AirRunnerParameters)flexRunnerParameters, flexSdk, isDebug)
                       : checkFlexParams(module, flexRunnerParameters);

    if (ok) {
      if (isDebug) {
        checkDebugInfoEnabled((FlexRunConfiguration)runProfile);
      }

      if (needToCheckThatCompilationEnabled(flexRunnerParameters)) {
        checkIfCompilationEnabled(module, (FlexRunConfiguration)runProfile, isDebug);
      }

      FlashPlayerTrustUtil.trustSwfIfNeeded(project, isDebug, flexRunnerParameters);

      return doLaunch(project, executor, state, contentToReuse, env, flexSdk, flexRunnerParameters);
    }

    return null;
  }

  private static boolean needToCheckThatCompilationEnabled(final FlexRunnerParameters parameters) {
    return parameters instanceof FlexUnitRunnerParameters
           || (parameters instanceof AirMobileRunnerParameters &&
               ((AirMobileRunnerParameters)parameters).getAirMobileRunMode() == AirMobileRunMode.MainClass)
           || (parameters instanceof AirRunnerParameters &&
               ((AirRunnerParameters)parameters).getAirRunMode() == AirRunnerParameters.AirRunMode.MainClass)
           || (parameters.getRunMode() == FlexRunnerParameters.RunMode.MainClass);
  }

  private static void checkIfCompilationEnabled(final Module module, final FlexRunConfiguration runProfile, final boolean isDebug) {
    final FlexBuildConfiguration config = FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module).iterator().next();
    if (!config.DO_BUILD) {
      showIdeBuilderTurnedOffWarning(module, isDebug);
    }
    else {
      final CompileStepBeforeRun.MakeBeforeRunTask runTask =
        RunManagerEx.getInstanceEx(module.getProject()).getBeforeRunTask(runProfile, CompileStepBeforeRun.ID);
      final boolean isMakeBeforeRun = runTask != null && runTask.isEnabled();
      if (!isMakeBeforeRun) {
        for (final RunnerAndConfigurationSettings settings : RunManagerEx.getInstanceEx(module.getProject())
          .getConfigurationSettings(runProfile.getType())) {
          if (settings.getConfiguration() == runProfile) {
            showCompileBeforeRunTurnedOffWarning(module.getProject(), settings, isDebug);
            break;
          }
        }
      }
    }
  }

  private static void showIdeBuilderTurnedOffWarning(final Module module, final boolean isDebug) {
    final FlexFacet facet = (module.getModuleType() instanceof FlexModuleType)
                            ? null : FacetManager.getInstance(module).getFacetByType(FlexFacet.ID);
    final HyperlinkListener listener = new HyperlinkAdapter() {
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        final ProjectStructureConfigurable projectStructureConfigurable = ProjectStructureConfigurable.getInstance(module.getProject());
        ShowSettingsUtil.getInstance().editConfigurable(module.getProject(), projectStructureConfigurable, new Runnable() {
          public void run() {
            if (facet != null) {
              projectStructureConfigurable.select(facet, true);
            }
            else {
              projectStructureConfigurable.select(module.getName(), FlexBundle.message("flex.compiler.settings"), true);
            }
          }
        });
      }
    };
    ToolWindowManager.getInstance(module.getProject())
      .notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.WARNING,
                       FlexBundle.message("run.when.ide.builder.turned.off", module.getName()), null, listener);
  }

  private static void showCompileBeforeRunTurnedOffWarning(final Project project,
                                                           final RunnerAndConfigurationSettings configuration,
                                                           final boolean isDebug) {
    final HyperlinkListener listener = new HyperlinkAdapter() {
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        RunDialog.editConfiguration(project, configuration, FlexBundle.message("edit.configuration.title"));
      }
    };
    ToolWindowManager.getInstance(project).notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.WARNING,
                                                           FlexBundle.message("run.when.compile.before.run.turned.off"), null, listener);
  }

  private static void checkDebugInfoEnabled(final FlexRunConfiguration runConfiguration) {
    final Project project = runConfiguration.getProject();
    final FlexRunnerParameters params = runConfiguration.getRunnerParameters();

    final CompileStepBeforeRun.MakeBeforeRunTask runTask =
      RunManagerEx.getInstanceEx(project).getBeforeRunTask(runConfiguration, CompileStepBeforeRun.ID);
    final boolean isMakeBeforeRun = runTask != null && runTask.isEnabled();

    if (!isMakeBeforeRun) {
      return;
    }

    final VirtualFile configFile = getCustomConfigUsedForCompilation(project, params);
    if (configFile != null) {
      showWarningIfDebugNotEnabled(project, configFile);
    }
    else {

      final FlexCompilerProjectConfiguration compilerConfiguration = FlexCompilerProjectConfiguration.getInstance(project);

      if (!compilerConfiguration.SWF_DEBUG_ENABLED) {
        if (params.getRunMode() != FlexRunnerParameters.RunMode.ConnectToRunningFlashPlayer || params instanceof AirRunnerParameters) {
          final HyperlinkListener listener = new HyperlinkAdapter() {
            protected void hyperlinkActivated(final HyperlinkEvent e) {
              final FlexCompilerProjectSettingsFactory factory =
                Extensions.findExtension(CompilerSettingsFactory.EP_NAME, project, FlexCompilerProjectSettingsFactory.class);
              ShowSettingsUtil.getInstance().editConfigurable(project, factory.create(project));
            }
          };

          ToolWindowManager.getInstance(project)
            .notifyByBalloon(ToolWindowId.DEBUG, MessageType.WARNING, FlexBundle.message("compilation.without.debug.info"), null, listener);
        }
      }
    }
  }

  /**
   * Looks if custom config file was used for compilation of the swf file that will be run.
   */
  @Nullable
  private static VirtualFile getCustomConfigUsedForCompilation(final Project project, final FlexRunnerParameters params) {
    String[] swfFileNames = null;
    if (!(params instanceof AirRunnerParameters) && params.getRunMode() == FlexRunnerParameters.RunMode.HtmlOrSwfFile) {
      final String[] swfFilePaths = FlashPlayerTrustUtil.getSwfFilesCanonicalPaths(project, params);
      swfFileNames = new String[swfFilePaths.length];
      for (int i = 0; i < swfFilePaths.length; i++) {
        swfFileNames[i] = swfFilePaths[i].substring(FileUtil.toSystemIndependentName(swfFilePaths[i]).lastIndexOf("/") + 1);
      }
    }
    else if (params instanceof AirRunnerParameters &&
             !(params instanceof FlexUnitRunnerParameters) &&
             ((AirRunnerParameters)params).getAirRunMode() == AirRunnerParameters.AirRunMode.AppDescriptor) {
      final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(((AirRunnerParameters)params).getAirDescriptorPath());
      if (descriptorFile != null) {
        try {
          final String swfFilePath = FlexUtils.findXMLElement(descriptorFile.getInputStream(), "<application><initialWindow><content>");
          if (swfFilePath != null) {
            swfFileNames = new String[]{swfFilePath.substring(FileUtil.toSystemIndependentName(swfFilePath).lastIndexOf("/") + 1)};
          }
        }
        catch (IOException e) {/*ignore*/}
      }
    }

    if (swfFileNames != null && swfFileNames.length > 0) {
      final Module module = ModuleManager.getInstance(project).findModuleByName(params.getModuleName());
      for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
        if (config.DO_BUILD && config.OUTPUT_TYPE.equals(FlexBuildConfiguration.APPLICATION) && config.USE_CUSTOM_CONFIG_FILE) {
          final VirtualFile configFile = LocalFileSystem.getInstance().findFileByPath(config.CUSTOM_CONFIG_FILE);
          if (configFile != null) {
            try {
              final String outputFilePath = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><output>");
              if (outputFilePath != null) {
                for (final String swfFileName : swfFileNames) {
                  if (outputFilePath.endsWith(swfFileName)) {
                    return configFile;
                  }
                }
              }
            }
            catch (IOException e) {/*ignore*/}
          }
        }
      }
      return null;
    }

    return null;
  }

  private static void showWarningIfDebugNotEnabled(final Project project, final @NotNull VirtualFile configFile) {
    try {
      final String debug = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><compiler><debug>");
      if (!"true".equals(debug)) {
        final HyperlinkListener listener = new HyperlinkAdapter() {
          protected void hyperlinkActivated(final HyperlinkEvent e) {
            FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, configFile), true);
          }
        };
        ToolWindowManager.getInstance(project)
          .notifyByBalloon(ToolWindowId.DEBUG, MessageType.WARNING,
                           FlexBundle.message("custom.config.without.debug.info", configFile.getName()), null, listener);
      }
    }
    catch (IOException e) {/**/}
  }

  private static boolean checkAirParams(final AirRunnerParameters airRunnerParameters, final Sdk sdk, final boolean isDebug)
    throws CantRunException {

    if (airRunnerParameters instanceof AirMobileRunnerParameters) {
      final AirMobileRunnerParameters mobileParams = (AirMobileRunnerParameters)airRunnerParameters;
      if (mobileParams.getAirMobileRunMode() == AirMobileRunMode.ExistingPackage) {
        if (mobileParams.getAirMobileRunTarget() == AirMobileRunTarget.Emulator) {
          throw new CantRunException("Can't launch AIR mobile package on emulator");
        }
      }

      if (isDebug && mobileParams.getAirMobileRunTarget() == AirMobileRunTarget.AndroidDevice &&
          mobileParams.getDebugTransport() == AirMobileDebugTransport.USB) {
        try {
          final Sdk debugSdk = FlexDebugProcess.getDebuggerSdk(mobileParams, sdk);
          final String version = debugSdk.getVersionString();
          if (StringUtil.isNotEmpty(version) &&
              Character.isDigit(version.charAt(0)) &&    // ignore "unknown" version
              StringUtil.compareVersionNumbers(version, "4.5") < 0) {
            throw new CantRunException(FlexBundle.message("debugger.from.sdk.does.not.support.usb", debugSdk.getName()));
          }
        }
        catch (IOException e) {
          throw new CantRunException(e.getMessage());
        }
      }

      return true;
    }

    if (LocalFileSystem.getInstance().findFileByPath(airRunnerParameters.getAirDescriptorPath()) == null) {
      throw new CantRunException(
        FlexBundle.message("air.descriptor.not.found", airRunnerParameters.getAirDescriptorPath().replace('/', File.separatorChar)));
    }
    // everything is already checked for run configuration
    return true;
  }

  private static boolean checkFlexParams(final Module module, final FlexRunnerParameters flexRunnerParameters) throws CantRunException {
    switch (flexRunnerParameters.getRunMode()) {
      case HtmlOrSwfFile:
        VirtualFile originalFile = null;
        if (flexRunnerParameters.getHtmlOrSwfFilePath().length() > 0) {
          originalFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(flexRunnerParameters.getHtmlOrSwfFilePath());
        }
        VirtualFile file = originalFile;

        if (file == null) {
          final String[] availableHtmlWrappersAndSwfs = collectHTMLWrappersAndSwfFilesFromOutputFolder(module);

          if (availableHtmlWrappersAndSwfs.length == 0) {
            throw new CantRunException(FlexBundle.message("no.file.to.run"));
          }
          else {
            final String missingPath = LocalFileSystem.getInstance().extractPresentableUrl(flexRunnerParameters.getHtmlOrSwfFilePath());
            int selected = Messages.showChooseDialog(FlexBundle.message("choose.swf.html.wrapper.file.to.run", missingPath),
                                                     FlexBundle.message("choose.file.to.run.title"), availableHtmlWrappersAndSwfs,
                                                     availableHtmlWrappersAndSwfs[0], Messages.getQuestionIcon());

            if (selected != -1) {
              file = LocalFileSystem.getInstance().findFileByPath(availableHtmlWrappersAndSwfs[selected]);
            }
          }
        }

        if (file == null) throw new CantRunException(FlexBundle.message("no.file.to.run"));
        if (file != originalFile) flexRunnerParameters.setHtmlOrSwfFilePath(file.getPath());

        break;
      case Url:
        final String flexUrl = flexRunnerParameters.getUrlToLaunch();
        final Ref<IOException> ioExceptionRef = new Ref<IOException>();

        final Runnable inProgressRunnable = new Runnable() {
          public void run() {
            final Semaphore semaphore = new Semaphore();
            semaphore.down();

            final Runnable urlCheckRunnable = new Runnable() {
              public void run() {
                try {
                  final URL url = new URL(flexUrl);
                  final URLConnection urlConnection = url.openConnection();
                  urlConnection.setConnectTimeout(URL_CHECK_TIMEOUT);
                  final InputStream inputStream;
                  try {
                    inputStream = urlConnection.getInputStream();
                    inputStream.close();
                  }
                  catch (IllegalArgumentException e) {
                    // Looks like JDK bug (some versions). IAE could be thrown by getInputStream() for malformed URL like "http:"
                    throw new MalformedURLException(FlexBundle.message("bad.url", flexUrl));
                  }
                  catch (NullPointerException e) {
                    // Looks like JDK bug (other versions :)). NPE could be thrown by getInputStream() for malformed URL like "http:"
                    throw new MalformedURLException(FlexBundle.message("bad.url", flexUrl));
                  }
                }
                catch (SSLHandshakeException e) {
                  // Server is alive, but some problems with certificate. Ignore.
                }
                catch (IOException e) {
                  final String message = e.getMessage();
                  if (message == null || !message.contains("HTTP response code: 401")) {
                    ioExceptionRef.set(e);
                  }
                }
                finally {
                  semaphore.up();
                }
              }
            };

            ApplicationManager.getApplication().executeOnPooledThread(urlCheckRunnable);
            final boolean urlCheckThreadFinished = semaphore.waitFor(URL_CHECK_TIMEOUT);
            ProgressManager.checkCanceled();
            if (!urlCheckThreadFinished) {
              ioExceptionRef.set(new IOException(FlexBundle.message("no.response.from.server", URL_CHECK_TIMEOUT / 1000)));
            }
          }
        };

        final boolean ok = ProgressManager.getInstance()
          .runProcessWithProgressSynchronously(inProgressRunnable, FlexBundle.message("checking.url", flexUrl), true, module.getProject());
        if (!ok) {
          return false;
        }
        if (!ioExceptionRef.isNull()) {
          final int choice = Messages
            .showYesNoDialog(FlexBundle.message("remote.url.is.not.accessible.message", flexUrl, ioExceptionRef.get().toString()),
                             FlexBundle.message("remote.url.is.not.accessible.title"), Messages.getQuestionIcon());
          if (choice != 0) {
            return false;
          }
        }
        break;
      case MainClass:
        // A sort of hack. HtmlOrSwfFilePath field is disabled in UI for MainClass-based run configuration. But it is set correctly in RunMainClassPrecompileTask.execute()
        if (LocalFileSystem.getInstance().refreshAndFindFileByPath(flexRunnerParameters.getHtmlOrSwfFilePath()) == null) {
          throw new CantRunException(FlexBundle.message("file.not.found", flexRunnerParameters.getHtmlOrSwfFilePath()));
        }
        break;
    }
    return true;
  }

  @Nullable
  protected abstract RunContentDescriptor doLaunch(final Project project,
                                                   final Executor executor,
                                                   final RunProfileState state,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final Sdk flexSdk,
                                                   final FlexRunnerParameters flexRunnerParameters) throws ExecutionException;

  public static void processFilesUnderRoots(final Project project, Processor<VirtualFile> leafFileProcessor) {
    for (VirtualFile root : ProjectRootManager.getInstance(project).getContentRoots()) {
      if (!processFilesUnderRoot(root, leafFileProcessor)) return;
    }
  }

  public static boolean processFilesUnderRoot(final VirtualFile root, Processor<VirtualFile> leafFileProcessor) {
    if (root.isDirectory()) {
      for (VirtualFile file : root.getChildren()) {
        if (!processFilesUnderRoot(file, leafFileProcessor)) return false;
      }
      return true;
    }
    else {
      return leafFileProcessor.process(root);
    }
  }

  public static String[] collectHTMLWrappersAndSwfFilesFromOutputFolder(final Module module) {
    final List<String> fileNames = new ArrayList<String>();
    final Set<String> pathsToSearchIn = new HashSet<String>();

    for (final FlexBuildConfiguration configuration : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
      pathsToSearchIn.add(configuration.getCompileOutputPath());
    }

    for (final String path : pathsToSearchIn) {
      final VirtualFile outputFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
      if (outputFolder != null) {
        processFilesUnderRoot(outputFolder, new Processor<VirtualFile>() {
          public boolean process(final VirtualFile virtualFile) {
            if (FlexUtils.isSwfExtension(virtualFile.getExtension()) ||
                (FlexUtils.isHtmlExtension(virtualFile.getExtension()) && FlexUtils.htmlFileLooksLikeSwfWrapper(virtualFile))) {
              fileNames.add(virtualFile.getPresentableUrl());
            }
            return true;
          }
        });
      }
    }

    return ArrayUtil.toStringArray(fileNames);
  }

  public static ExecutionConsole createFlexUnitRunnerConsole(Project project,
                                                             ExecutionEnvironment env,
                                                             ProcessHandler processHandler,
                                                             Executor executor)
    throws ExecutionException {
    final FlexUnitRunConfiguration runConfiguration = (FlexUnitRunConfiguration)env.getRunProfile();
    final FlexUnitConsoleProperties consoleProps = new FlexUnitConsoleProperties(runConfiguration, executor);
    final BaseTestsOutputConsoleView consoleView = SMTestRunnerConnectionUtil
      .attachRunner("FlexUnit", processHandler, consoleProps, env.getRunnerSettings(), env.getConfigurationSettings());
    consoleView.addMessageFilter(new FlexStackTraceFilter(project));
    Disposer.register(project, consoleView);
    return consoleView;
  }

  public static boolean isRunOnDevice(final FlexRunnerParameters params) {
    return params instanceof AirMobileRunnerParameters &&
           ((AirMobileRunnerParameters)params).getAirMobileRunTarget() != AirMobileRunTarget.Emulator;
  }

  public static boolean isRunAsAir(final FlexRunnerParameters params) {
    return params instanceof FlexUnitRunnerParameters
           ? ((FlexUnitRunnerParameters)params).isRunAsAir()
           : params instanceof AirRunnerParameters;
  }

  public static void launchWithSelectedApplication(final String urlOrPath, final FlexRunnerParameters parameters) {
    switch (parameters.getLauncherType()) {
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
            BrowsersConfiguration
              .launchBrowser(parameters.getBrowserFamily(),
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
        final String playerPath = parameters.getPlayerPath();
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

  public static boolean packAndInstallToDevice(final Project project,
                                               final Sdk flexSdk,
                                               final AirMobileRunnerParameters params,
                                               final String swfPath,
                                               final String applicationId,
                                               final boolean isDebug) {
    if (params.getAirMobileRunTarget() == AirMobileRunTarget.AndroidDevice) {
      final Module module = ModuleManager.getInstance(project).findModuleByName(params.getModuleName());
      assert module != null;
      final MobileAirPackageParameters packageParameters = createAndroidPackageParams(flexSdk, swfPath, params, isDebug);
      final String apkPath = packageParameters.INSTALLER_FILE_LOCATION + "/" + packageParameters.INSTALLER_FILE_NAME;

      final String adtVersion;
      return (adtVersion = MobileAirUtil.getAdtVersion(project, flexSdk)) != null
             && MobileAirUtil.checkAdtVersion(module, flexSdk, adtVersion)
             && MobileAirUtil.checkAirRuntimeOnDevice(project, flexSdk, adtVersion)
             && MobileAirUtil.ensureCertificateExists(project, flexSdk)
             && MobileAirUtil.packageApk(project, packageParameters)
             && MobileAirUtil.installApk(project, flexSdk, apkPath, applicationId);
    }
    else {
      assert false;
    }

    return false;
  }

  public static boolean installToDevice(final Project project,
                                        final Sdk flexSdk,
                                        final AirMobileRunnerParameters params,
                                        final String applicationId) {
    if (params.getAirMobileRunTarget() == AirMobileRunTarget.AndroidDevice) {
      assert params.getAirMobileRunMode() == AirMobileRunMode.ExistingPackage;
      final Module module = ModuleManager.getInstance(project).findModuleByName(params.getModuleName());
      assert module != null;

      final String adtVersion;
      return (adtVersion = MobileAirUtil.getAdtVersion(project, flexSdk)) != null
             && MobileAirUtil.checkAdtVersion(module, flexSdk, adtVersion)
             && MobileAirUtil.checkAirRuntimeOnDevice(project, flexSdk, adtVersion)
             && MobileAirUtil.installApk(project, flexSdk, params.getExistingPackagePath(), applicationId);
    }
    else {
      assert false;
    }

    return false;
  }

  public static Pair<String, String> getSwfPathAndApplicationId(final AirMobileRunnerParameters params) {
    assert params.getAirMobileRunMode() != AirMobileRunMode.ExistingPackage;

    final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(params.getAirDescriptorPath());
    if (descriptorFile != null) {
      try {
        final String swfPath = FlexUtils.findXMLElement(descriptorFile.getInputStream(), "<application><initialWindow><content>");
        final String applicationId = FlexUtils.findXMLElement(descriptorFile.getInputStream(), "<application><id>");
        return Pair.create(swfPath, applicationId);
      }
      catch (IOException e) {/*ignore*/}
    }
    return Pair.create(null, null);
  }

  private static MobileAirPackageParameters createAndroidPackageParams(final Sdk flexSdk,
                                                                       final String swfPath,
                                                                       final AirMobileRunnerParameters params,
                                                                       boolean isDebug) {
    String swfName = "";
    String outputDirPath = "";
    final int lastSlashIndex = FileUtil.toSystemIndependentName(swfPath).lastIndexOf('/');
    final String suffix = ".swf";
    if (swfPath.toLowerCase().endsWith(suffix) && lastSlashIndex < swfPath.length() - suffix.length()) {
      swfName = swfPath.substring(lastSlashIndex + 1);
      outputDirPath = params.getAirRootDirPath() + (lastSlashIndex == -1 ? "" : "/" + swfPath.substring(0, lastSlashIndex));
    }

    final List<FilePathAndPathInPackage> files = AirInstallerParametersBase.cloneList(params.getFilesToPackage());
    files.add(0, new FilePathAndPathInPackage(params.getAirRootDirPath() + "/" + swfPath, swfName));

    final AndroidPackageType packageType = isDebug
                                           ? params.getDebugTransport() == AirMobileDebugTransport.Network
                                             ? AndroidPackageType.DebugOverNetwork
                                             : AndroidPackageType.DebugOverUSB
                                           : AndroidPackageType.NoDebug;
    return new MobileAirPackageParameters(MobilePlatform.Android,
                                          packageType,
                                          IOSPackageType.DebugOverNetwork,
                                          true,
                                          flexSdk,
                                          params.getAirDescriptorPath(),
                                          params.getMobilePackageFileName(),
                                          outputDirPath,
                                          files,
                                          MobileAirUtil.getLocalHostAddress(),
                                          params.getUsbDebugPort(),
                                          "",
                                          "",
                                          MobileAirUtil.getTempKeystorePath(),
                                          MobileAirUtil.PKCS12_KEYSTORE_TYPE,
                                          MobileAirUtil.TEMP_KEYSTORE_PASSWORD,
                                          "",
                                          "",
                                          "",
                                          "");
  }

  @Nullable
  public static RunContentDescriptor launchOnDevice(final Project project,
                                                    final Sdk flexSdk,
                                                    final AirMobileRunnerParameters params,
                                                    final boolean isDebug) {
    final String appId = params.getAirMobileRunMode() == AirMobileRunMode.ExistingPackage
                         ? MobileAirUtil.getAppIdFromPackage(params.getExistingPackagePath())
                         : getSwfPathAndApplicationId(params).second;
    if (appId == null) {
      Messages.showErrorDialog(project, "Failed to get application id", "Error");
    }

    return launchOnDevice(project, flexSdk, params, appId, isDebug);
  }

  @Nullable
  public static RunContentDescriptor launchOnDevice(final Project project,
                                                    final Sdk flexSdk,
                                                    final AirMobileRunnerParameters params,
                                                    final String applicationId,
                                                    final boolean isDebug) {
    switch (params.getAirMobileRunTarget()) {
      case Emulator:
        assert false;
        break;
      case AndroidDevice:
        if (MobileAirUtil.launchAndroidApplication(project, flexSdk, applicationId)) {
          ToolWindowManager.getInstance(project).notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.INFO,
                                                                 FlexBundle.message("android.application.launched"));
        }
        break;
    }

    return null;
  }
}
