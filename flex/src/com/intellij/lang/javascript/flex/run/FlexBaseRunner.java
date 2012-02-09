package com.intellij.lang.javascript.flex.run;

import com.intellij.CommonBundle;
import com.intellij.compiler.CompilerSettingsFactory;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.*;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.RunDialog;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.facet.FacetManager;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.actions.AirSigningOptions;
import com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectSettingsFactory;
import com.intellij.lang.javascript.flex.debug.FlexDebugProcess;
import com.intellij.lang.javascript.flex.debug.FlexDebugRunner;
import com.intellij.lang.javascript.flex.flexunit.*;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
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
import com.intellij.util.PathUtil;
import com.intellij.util.Processor;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
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
            FlashPlayerTrustUtil.updateTrustedStatus(module.getProject(), params.isRunTrusted(), false, canonicalPath);
          }
          catch (IOException e) {/**/}
        }

        return launchFlexIdeConfig(module, bc, params, executor, state, contentToReuse, env);
      }
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }

    // todo remove following dead code
    final FlexRunnerParameters flexRunnerParameters = (((FlexRunConfiguration)runProfile)).getRunnerParameters();

    final Module module = ModuleManager.getInstance(project).findModuleByName(flexRunnerParameters.getModuleName());
    final Sdk flexSdk = module == null ? null : FlexUtils.getSdkForActiveBC(module);
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

      //FlashPlayerTrustUtil.updateTrustedStatus(project, isDebug, flexRunnerParameters);

      return doLaunch(project, executor, state, contentToReuse, env, flexSdk, flexRunnerParameters);
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
    final FlexFacet facet = (ModuleType.get(module) instanceof FlexModuleType)
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

    final VirtualFile configFile = null; //getCustomConfigUsedForCompilation(project, params);
    if (configFile != null) {
      showWarningIfDebugNotEnabled(project, configFile);
    }
    else {

      final FlexCompilerProjectConfiguration compilerConfiguration = FlexCompilerProjectConfiguration.getInstance(project);

      //if (!compilerConfiguration.SWF_DEBUG_ENABLED) {
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
      //}
    }
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
                                                             Executor executor) throws ExecutionException {
    final RunProfile runConfiguration = env.getRunProfile();
    final FlexStackTraceFilter stackTraceFilter = new FlexStackTraceFilter(project);
    final FlexUnitConsoleProperties consoleProps = runConfiguration instanceof FlexUnitRunConfiguration
                                                   ? new FlexUnitConsoleProperties((FlexUnitRunConfiguration)runConfiguration, executor)
                                                   : new FlexUnitConsoleProperties(((NewFlexUnitRunConfiguration)runConfiguration),
                                                                                   executor);
    consoleProps.addStackTraceFilter(stackTraceFilter);

    final BaseTestsOutputConsoleView consoleView = SMTestRunnerConnectionUtil
      .createAndAttachConsole("FlexUnit", processHandler, consoleProps, env.getRunnerSettings(), env.getConfigurationSettings());
    consoleView.addMessageFilter(stackTraceFilter);
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
                                           ? params.getDebugTransport() == AirMobileDebugTransport.Network
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

    launchOnAndroidDevice(project, flexSdk, appId, isDebug);
    return null;
  }

  public static void launchOnAndroidDevice(final Project project, final Sdk flexSdk, final String applicationId, final boolean isDebug) {
    if (MobileAirUtil.launchAndroidApplication(project, flexSdk, applicationId)) {
      ToolWindowManager.getInstance(project).notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.INFO,
                                                             FlexBundle.message("android.application.launched"));
    }
  }

  public static GeneralCommandLine createAdlCommandLine(final BCBasedRunnerParameters params,
                                                        final FlexIdeBuildConfiguration bc) throws CantRunException {
    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      throw new CantRunException(FlexBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), params.getModuleName()));
    }

    final GeneralCommandLine commandLine = new GeneralCommandLine();

    commandLine.setExePath(FileUtil.toSystemDependentName(sdk.getHomePath() + FlexSdkUtils.ADL_RELATIVE_PATH));

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
      assert flashParams.getMobileRunTarget() == AirMobileRunTarget.Emulator : flashParams.getMobileRunTarget();

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
