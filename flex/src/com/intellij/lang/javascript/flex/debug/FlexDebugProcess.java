package com.intellij.lang.javascript.flex.debug;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.idea.LoggerFactory;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.lang.javascript.flex.flexunit.*;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.SdkEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.run.*;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XValueMarkerProvider;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.AirMobileDebugTransport;
import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.AirMobileRunTarget;

/**
 * @author Maxim.Mossienko
 *         Date: Jan 22, 2008
 *         Time: 4:38:36 PM
 */
public class FlexDebugProcess extends XDebugProcess {
  private static final String TRACE_MARKER = "[trace] ";
  public static final String DEBUGGER_GROUP_ID = "Debugger";

  private boolean debugSessionInitialized;
  private final Process fdbProcess;
  private Process adlProcess;

  private final MyFdbOutputReader reader;
  private Alarm myOutputAlarm;

  private final String myDebuggerVersion;
  protected final String mySdkLocation;

  @NonNls static final String RESOLVED_BREAKPOINT_MARKER = "Resolved breakpoint ";
  @NonNls static final String BREAKPOINT_MARKER = "Breakpoint ";
  @NonNls private static final String FDB_MARKER = "(fdb) ";
  @NonNls private static final String WAITING_PLAYER_MARKER = "Waiting for Player to connect";
  @NonNls static final String ATTEMPTING_TO_RESOLVE_BREAKPOINT_MARKER = "Attempting to resolve breakpoint ";

  @NonNls private static final String ADL_PREFIX = "[AIR Debug Launcher]: ";

  private static boolean ourReportedAboutPossibleStartupFailure;
  private boolean myCheckForUnexpectedStartupStop;
  private Thread myDebuggerManagerThread;
  @NonNls static final String AMBIGUOUS_MATCHING_FILE_NAMES = "Ambiguous matching file names:";

  private final FlexBreakpointsHandler myBreakpointsHandler;
  @NonNls private static final String FAULT_MARKER = "[Fault] ";
  private static final Logger LOG = LoggerFactory.getInstance().getLoggerInstance(FlexDebugProcess.class.getName());
  private static boolean doSimpleTracing = ((ApplicationEx)ApplicationManager.getApplication()).isInternal();

  private Object myStackFrameEqualityObject;
  private Map<String, String> myQName2IdMap;

  private boolean myFileIdIsUpToDate = false;
  protected final BidirectionalMap<String, String> myFilePathToIdMap = new BidirectionalMap<String, String>();
  protected final Map<String, Collection<String>> myFileNameToPathsMap = new THashMap<String, Collection<String>>();
  protected final Map<String, Collection<VirtualFile>> myFileNameToFilesMap = new THashMap<String, Collection<VirtualFile>>();

  private String myFdbLaunchCommand;

  private final LinkedList<DebuggerCommand> commandsToWrite = new LinkedList<DebuggerCommand>() {
    @Override
    public synchronized DebuggerCommand removeFirst() {
      waitForData();
      return super.removeFirst();
    }

    @Override
    public synchronized void addFirst(final DebuggerCommand debuggerCommand) {
      super.addFirst(debuggerCommand);
      notify();
    }

    @Override
    public synchronized void addLast(final DebuggerCommand debuggerCommand) {
      super.addLast(debuggerCommand);
      notify();
    }

    // TODO: other methods

    private void waitForData() {
      try {
        while (size() == 0) {
          wait();
        }
      }
      catch (InterruptedException ex) {
        throw new RuntimeException(ex);
      }
    }
  };

  private boolean suspended;
  private boolean fdbWaitingForPlayerStateReached;
  private final boolean connectToRunningFlashPlayerMode;
  private boolean startupDone;
  private ConsoleView myConsoleView;
  private FlexUnitConnection myFlexUnitConnection;
  private SwfPolicyFileConnection myPolicyFileConnection;

  public FlexDebugProcess(final XDebugSession session, final FlexIdeBuildConfiguration config, final BCBasedRunnerParameters params)
    throws IOException {

    super(session);

    final SdkEntry sdkEntry = config.getDependencies().getSdkEntry();
    assert sdkEntry != null; // checked in FlexBaseRunner
    final String sdkHome = sdkEntry.getHomePath();

    final Library sdk = sdkEntry.findLibrary();
    myDebuggerVersion = StringUtil.notNullize(sdk != null ? FlexSdk.getFlexVersion(sdk) : null, "unknown");
    myBreakpointsHandler = new FlexBreakpointsHandler(this);
    mySdkLocation = FileUtil.toSystemIndependentName(sdkHome);

    final List<String> fdbLaunchCommand = FlexSdkUtils
      .getCommandLineForSdkTool(session.getProject(), sdkHome, null, getFdbClasspath(), "flex.tools.debugger.cli.DebugCLI", null);

    if (params instanceof FlexIdeRunnerParameters &&
        config.getTargetPlatform() == TargetPlatform.Mobile &&
        ((FlexIdeRunnerParameters)params).getMobileRunTarget() == AirMobileRunTarget.AndroidDevice &&
        ((FlexIdeRunnerParameters)params).getDebugTransport() == AirMobileDebugTransport.USB) {
      fdbLaunchCommand.add("-p");
      fdbLaunchCommand.add(String.valueOf(((FlexIdeRunnerParameters)params).getUsbDebugPort()));
    }

    fdbProcess = launchFdb(fdbLaunchCommand);

    if (params instanceof FlexIdeRunnerParameters) {
      connectToRunningFlashPlayerMode = false;
      final FlexIdeRunnerParameters appParams = (FlexIdeRunnerParameters)params;

      switch (config.getTargetPlatform()) {
        case Web:
          // todo support wrapper
          final String urlOrPath = appParams.isLaunchUrl() ? appParams.getUrl() : config.getOutputFilePath();
          sendCommand(new LaunchBrowserCommand(urlOrPath, appParams.getLauncherParameters()));
          break;
        case Desktop:
          sendAdlStartingCommand(config, appParams);
          break;
        case Mobile:
          switch (appParams.getMobileRunTarget()) {
            case Emulator:
              sendAdlStartingCommand(config, appParams);
              break;
            case AndroidDevice:
              final String appId =
                FlexBaseRunner.getApplicationId(FlexBaseRunner.getAirDescriptorPath(config, config.getAndroidPackagingOptions()));
              sendCommand(appParams.getDebugTransport() == AirMobileDebugTransport.Network
                          ? new StartAppOnAndroidDeviceCommand(FlexUtils.createFlexSdkWrapper(config), appId)
                          : new StartDebuggingCommand());
              break;
          }
      }
    }
    else if (params instanceof NewFlexUnitRunnerParameters) {
      connectToRunningFlashPlayerMode = false;
      final FlexUnitCommonParameters flexUnitParams = (FlexUnitCommonParameters)params;
      openFlexUnitConnections(flexUnitParams.getSocketPolicyPort(), flexUnitParams.getPort());
      final LauncherParameters launcherParams =
        new LauncherParameters(LauncherParameters.LauncherType.OSDefault, BrowsersConfiguration.BrowserFamily.FIREFOX, "");
      sendCommand(new LaunchBrowserCommand(config.getOutputFilePath(), launcherParams));
    }
    else {
      connectToRunningFlashPlayerMode = true;
      sendCommand(new StartDebuggingCommand());
    }

    reader = new MyFdbOutputReader(fdbProcess.getInputStream());

    startCommandProcessingThread();
  }

  public FlexDebugProcess(final XDebugSession session, final Sdk flexSdk, final FlexRunnerParameters flexRunnerParameters)
    throws IOException {

    super(session);

    final Sdk debuggerSdk = getDebuggerSdk(flexRunnerParameters, flexSdk);

    myDebuggerVersion = StringUtil.notNullize(debuggerSdk.getVersionString(), "unknown");
    myBreakpointsHandler = new FlexBreakpointsHandler(this);
    mySdkLocation = FileUtil.toSystemIndependentName(flexSdk.getHomePath());

    if (flexRunnerParameters instanceof FlexUnitRunnerParameters) {
      final FlexUnitRunnerParameters flexUnitParams = (FlexUnitRunnerParameters)flexRunnerParameters;
      openFlexUnitConnections(flexUnitParams.getSocketPolicyPort(), flexUnitParams.getPort());
    }

    final List<String> fdbLaunchCommand =
      FlexSdkUtils.getCommandLineForSdkTool(session.getProject(), debuggerSdk, getFdbClasspath(), "flex.tools.debugger.cli.DebugCLI", null);

    if (flexRunnerParameters instanceof AirMobileRunnerParameters
        && ((AirMobileRunnerParameters)flexRunnerParameters).getAirMobileRunTarget() == AirMobileRunTarget.AndroidDevice
        && ((AirMobileRunnerParameters)flexRunnerParameters).getDebugTransport() == AirMobileDebugTransport.USB) {
      fdbLaunchCommand.add("-p");
      fdbLaunchCommand.add(String.valueOf(((AirMobileRunnerParameters)flexRunnerParameters).getUsbDebugPort()));
    }

    fdbProcess = launchFdb(fdbLaunchCommand);

    final boolean runAsAir = FlexBaseRunner.isRunAsAir(flexRunnerParameters);
    connectToRunningFlashPlayerMode =
      !runAsAir && flexRunnerParameters.getRunMode() == FlexRunnerParameters.RunMode.ConnectToRunningFlashPlayer;

    if (runAsAir) {
      launchAir((AirRunnerParameters)flexRunnerParameters, flexSdk);
    }
    else {
      launchFlex(flexRunnerParameters);
    }

    reader = new MyFdbOutputReader(fdbProcess.getInputStream());

    startCommandProcessingThread();
  }

  private String getFdbClasspath() {
    String classpath = mySdkLocation + "/lib/fdb.jar";

    if (isDebuggerFromSdk3()) {
      classpath = FlexUtils.getPathToBundledJar("idea-fdb-3-fix.jar") + File.pathSeparator + classpath;
    }
    else if (isDebuggerFromSdk4()) {
      if (myDebuggerVersion.contains("14159") || myDebuggerVersion.contains("16076")) {
        classpath = FlexUtils.getPathToBundledJar("idea-fdb-4.0.0.14159-fix.jar") + File.pathSeparator + classpath;
      }
      else if (myDebuggerVersion.contains("20967") || myDebuggerVersion.contains("21328")) {
        classpath = FlexUtils.getPathToBundledJar("idea-fdb-4.5.0.20967-fix.jar") + File.pathSeparator + classpath;
      }
    }
    return classpath;
  }

  private void openFlexUnitConnections(final int socketPolicyPort, final int port) {
    try {
      myPolicyFileConnection = new SwfPolicyFileConnection();
      myPolicyFileConnection.open(socketPolicyPort);

      myFlexUnitConnection = new FlexUnitConnection();
      myFlexUnitConnection.addListener(new FlexUnitConnection.Listener() {
        public void statusChanged(FlexUnitConnection.ConnectionStatus status) {
          if (status == FlexUnitConnection.ConnectionStatus.CONNECTION_FAILED) {
            getSession().stop();
          }
        }

        public void onData(String line) {
          getProcessHandler().notifyTextAvailable(line + "\n", ProcessOutputTypes.STDOUT);
        }

        public void onFinish() {
          getProcessHandler().detachProcess();
        }
      });
      myFlexUnitConnection.open(port);
    }
    catch (ExecutionException e) {
      Notifications.Bus.notify(new Notification(
        DEBUGGER_GROUP_ID,
        FlexBundle.message("flex.debugger.startup.error"),
        FlexBundle.message("flexunit.startup.error", e.getMessage()),
        NotificationType.ERROR
      ), getSession().getProject());
      myFlexUnitConnection = null;
      myPolicyFileConnection = null;
    }
  }

  private Process launchFdb(final List<String> fdbLaunchCommand) throws IOException {
    ensureExecutable(fdbLaunchCommand.get(0));
    myFdbLaunchCommand = StringUtil.join(fdbLaunchCommand, new Function<String, String>() {
      public String fun(final String s) {
        return s.indexOf(' ') >= 0 && !(s.startsWith("\"") && s.endsWith("\"")) ? '\"' + s + '\"' : s;
      }
    }, " ");

    final Process process = Runtime.getRuntime().exec(ArrayUtil.toStringArray(fdbLaunchCommand));
    sendCommand(new ReadGreetingCommand()); // just to read copyrights and wait for "(fdb)"
    return process;
  }

  private void startCommandProcessingThread() {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        myDebuggerManagerThread = Thread.currentThread();
        synchronized (FlexDebugProcess.this) {
          if (!debugSessionInitialized) {
            try {
              FlexDebugProcess.this.wait();
            }
            catch (InterruptedException e) {
              // ignore
            }
          }
        }

        try {
          while (true) {
            processOneCommandLoop();
          }
        }
        catch (IOException ex) {
          myConsoleView.print(ex.toString(), ConsoleViewContentType.ERROR_OUTPUT);
          getProcessHandler().detachProcess();
          fdbProcess.destroy();
          LOG.warn(ex);
        }
        catch (InterruptedException e) {
          return;
        }
        catch (RuntimeException ex) {
          final Throwable throwable = ex.getCause();
          if (throwable instanceof InterruptedException) return;
          throw ex;
        }
        finally {
          try {
            fdbProcess.getInputStream().close();
          }
          catch (IOException ex) {
          }
        }
      }
    });
  }

  private void sendAdlStartingCommand(final FlexIdeBuildConfiguration config, final FlexIdeRunnerParameters params) throws IOException {
    try {
      sendCommand(new StartAirAppDebuggingCommand(FlexBaseRunner.createAdlCommandLine(params, config)));
    }
    catch (CantRunException e) {
      throw new IOException(e.getMessage());
    }
  }

  @Override
  public String getCurrentStateMessage() {
    return getSession().isStopped()
           ? XDebuggerBundle.message("debugger.state.message.disconnected")
           : startupDone
             ? XDebuggerBundle.message("debugger.state.message.connected")
             : fdbWaitingForPlayerStateReached
               ? FlexBundle.message("debugger.waiting.player")
               : FlexBundle.message("initializing.flex.debugger");
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new FlexDebuggerEditorsProvider();
  }

  public static Sdk getDebuggerSdk(final FlexRunnerParameters flexRunnerParameters, final Sdk flexSdk) throws IOException {
    final String debuggerSdkRaw = flexRunnerParameters.getDebuggerSdkRaw();
    if (debuggerSdkRaw.equals(FlexSdkComboBoxWithBrowseButton.MODULE_SDK_KEY)) {
      return flexSdk;
    }
    else {
      final Sdk sdk = ProjectJdkTable.getInstance().findJdk(debuggerSdkRaw);
      if (sdk == null || !(sdk.getSdkType() instanceof IFlexSdkType)) {
        throw new IOException(FlexBundle.message("debugger.sdk.not.found", debuggerSdkRaw));
      }
      return sdk;
    }
  }

  private void launchAir(final AirRunnerParameters airRunnerParameters, final Sdk flexSdk) throws IOException {
    if (airRunnerParameters instanceof AirMobileRunnerParameters) {
      final AirMobileRunnerParameters mobileParams = (AirMobileRunnerParameters)airRunnerParameters;
      switch (mobileParams.getAirMobileRunTarget()) {
        case Emulator:
          scheduleAdlLaunch(flexSdk, airRunnerParameters);
          break;
        case AndroidDevice:
          sendCommand(mobileParams.getDebugTransport() == AirMobileDebugTransport.Network
                      ? new StartAppOnAndroidDeviceCommand(flexSdk, mobileParams)
                      : new StartDebuggingCommand());
      }
    }
    else {
      scheduleAdlLaunch(flexSdk, airRunnerParameters);
    }
  }

  private void scheduleAdlLaunch(Sdk flexSdk, AirRunnerParameters airRunnerParameters) throws IOException {
    final String adlPath = FlexSdkUtils.getAdlPath(flexSdk);
    ensureExecutable(adlPath);
    final List<String> airLaunchCommand = new ArrayList<String>();
    airLaunchCommand.add(adlPath);

    final boolean needToRemoveAirRuntimeDir;
    final VirtualFile airRuntimeDirForFlexmojosSdk;
    if (flexSdk.getSdkType() instanceof FlexmojosSdkType) {
      final Pair<VirtualFile, Boolean> airRuntimeDirInfo = FlexSdkUtils.getAirRuntimeDirInfoForFlexmojosSdk(flexSdk);
      airRuntimeDirForFlexmojosSdk = airRuntimeDirInfo.first;
      needToRemoveAirRuntimeDir = airRuntimeDirInfo.second;
      airLaunchCommand.add("-runtime");
      airLaunchCommand.add(airRuntimeDirForFlexmojosSdk.getPath());
    }
    else {
      needToRemoveAirRuntimeDir = false;
      airRuntimeDirForFlexmojosSdk = null;
    }

    if (airRunnerParameters instanceof AirMobileRunnerParameters) {
      final AirMobileRunnerParameters p = (AirMobileRunnerParameters)airRunnerParameters;
      switch (p.getAirMobileRunTarget()) {
        case Emulator:
          airLaunchCommand.add("-profile");
          airLaunchCommand.add("mobileDevice");

          airLaunchCommand.add("-screensize");
          final String adlAlias = p.getEmulator().adlAlias;
          if (adlAlias != null) {
            airLaunchCommand.add(adlAlias);
          }
          else {
            airLaunchCommand.add(
              p.getScreenWidth() + "x" + p.getScreenHeight() + ":" + p.getFullScreenWidth() + "x" + p.getFullScreenHeight());
          }
          break;
        case AndroidDevice:
          assert false;
          break;
      }
    }

    final String adlOptions = airRunnerParameters.getAdlOptions();
    if (!StringUtil.isEmptyOrSpaces(adlOptions)) {
      airLaunchCommand.addAll(StringUtil.split(adlOptions, " "));
    }
    airLaunchCommand.add(airRunnerParameters.getAirDescriptorPath());
    airLaunchCommand.add(airRunnerParameters.getAirRootDirPath());
    final String programParameters = airRunnerParameters.getAirProgramParameters();
    if (!StringUtil.isEmptyOrSpaces(programParameters)) {
      airLaunchCommand.add("--");
      airLaunchCommand.addAll(StringUtil.split(programParameters, " "));
    }
    sendCommand(new StartAirAppDebuggingCommand(ArrayUtil.toStringArray(airLaunchCommand),
                                                needToRemoveAirRuntimeDir ? airRuntimeDirForFlexmojosSdk : null));
  }

  private void launchFlex(final FlexRunnerParameters flexRunnerParameters) throws IOException {
    final FlexRunnerParameters.RunMode runMode = flexRunnerParameters.getRunMode();

    final String url = runMode == FlexRunnerParameters.RunMode.HtmlOrSwfFile
                       ? flexRunnerParameters.getHtmlOrSwfFilePath()
                       : runMode == FlexRunnerParameters.RunMode.Url
                         ? flexRunnerParameters.getUrlToLaunch()
                         : runMode == FlexRunnerParameters.RunMode.MainClass
                           // A sort of hack. HtmlOrSwfFilePath field is disabled in UI for MainClass-based run configuration. But it is set correctly in RunMainClassPrecompileTask.execute()
                           ? flexRunnerParameters.getHtmlOrSwfFilePath()
                           // launch nothing if runMode == FlexRunnerParameters.RunMode.ConnectToRunningFlashPlayer
                           : null;
    final LauncherParameters launcherParameters = new LauncherParameters(flexRunnerParameters.getLauncherType(),
                                                                         flexRunnerParameters.getBrowserFamily(),
                                                                         flexRunnerParameters.getPlayerPath());
    sendCommand(url == null ? new StartDebuggingCommand() : new LaunchBrowserCommand(url, launcherParameters));
  }

  private static final Set<String> ourAlreadyMadeExecutable = new THashSet<String>();

  private static synchronized void ensureExecutable(String path) {
    if (!SystemInfo.isWindows && !ourAlreadyMadeExecutable.contains(path)) {
      try {
        ourAlreadyMadeExecutable.add(path);
        Runtime.getRuntime().exec(new String[]{"chmod", "+x", path});
      }
      catch (IOException ex) {
        log(ex);
      }
    }
  }

  private void processOneCommandLoop() throws IOException, InterruptedException {
    assert Thread.currentThread() == myDebuggerManagerThread;
    final DebuggerCommand command = postCommand();
    if (command == null) return;
    boolean explicitlyContinueRead = false;

    do {
      final CommandOutputProcessingType outputProcessingType = command.getOutputProcessingMode();
      if (outputProcessingType == CommandOutputProcessingType.NO_PROCESSING ||
          (outputProcessingType == CommandOutputProcessingType.DEFAULT_PROCESSING && !reader.hasSomeDataPending())) {
        return;
      }

      if (myCheckForUnexpectedStartupStop && !(command instanceof DumpOutputCommand)) {
        myCheckForUnexpectedStartupStop = false;
        ourReportedAboutPossibleStartupFailure = true;
      }

      @NonNls String commandOutput = null;
      try {
        commandOutput = command.read(this);
      }
      catch (IOException e) {
        if (!(command instanceof QuitCommand)) {
          throw e;
        }
      }

      if (command instanceof QuitCommand) {
        Thread.currentThread().interrupt();  // request to finish
      }
      if (commandOutput == null) break;

      commandOutput = commandOutput.trim();
      log(commandOutput);

      if (outputProcessingType == CommandOutputProcessingType.SPECIAL_PROCESSING) {
        log("Processed by " + command);
        if (command.onTextAvailable(commandOutput) == CommandOutputProcessingMode.DONE) break;
        explicitlyContinueRead = true;
        continue;
      }

      ResponseLineIterator iterator = new ResponseLineIterator(commandOutput);

      boolean toInsertContinue = false;
      boolean encounteredNonsuspendableBreakpoint = false;
      int index;

      while (iterator.hasNext()) {
        final String line = iterator.next();

        if (line.indexOf("Additional ActionScript code has been loaded") != -1) {
          if (!suspended) reader.readLine(false);
          myFileIdIsUpToDate = false;
        }
        else if ((index = line.indexOf(BREAKPOINT_MARKER)) != -1 && line.indexOf(" created") == -1) { // TODO: move to break point handler
          // Breakpoint 1, aaa() at A.mxml:14
          try {
            final int from = index + BREAKPOINT_MARKER.length();

            // Breakpoint 1, aaa() at A.mxml:14
            // Breakpoint 2: file ConfigurationService.as
            // Breakpoint 3 at 0xFFF
            int endOfBreakpointIndexPosition = line.indexOf(',', from);
            final int colonIndex = line.indexOf(':', from);
            final int spaceIndex = line.indexOf(' ', from);

            if (endOfBreakpointIndexPosition != -1) {
              if (colonIndex != -1) {
                endOfBreakpointIndexPosition = Math.min(colonIndex, endOfBreakpointIndexPosition);
              }
              if (spaceIndex != -1) {
                endOfBreakpointIndexPosition = Math.min(spaceIndex, endOfBreakpointIndexPosition);
              }
              index = Integer.parseInt(line.substring(from, endOfBreakpointIndexPosition));
              final XLineBreakpoint<XBreakpointProperties> breakpoint = myBreakpointsHandler.getBreakpointByIndex(index);

              if (breakpoint != null) {
                FlexSuspendContext suspendContext = new FlexSuspendContext(new FlexStackFrame(this, breakpoint.getSourcePosition()));
                boolean suspend = getSession().breakpointReached(breakpoint, suspendContext);
                final VirtualFile file = breakpoint.getSourcePosition().getFile();

                final String shortName = file.getName();
                addToMap(myFileNameToFilesMap, shortName, file);

                if (!suspend) {
                  encounteredNonsuspendableBreakpoint = true;
                  toInsertContinue = true;
                }
              }
              else {
                insertCommand(myBreakpointsHandler.new RemoveBreakpointCommand(index, breakpoint));  // run to cursor break point
              }
            }
          }
          catch (NumberFormatException ex) {
            log(ex);
          }
        }
        else if (line.length() > 0 &&
                 Character.isDigit(line.charAt(0))) {  // we are on new location: e.g. " 119           trace('\x30 \123')"
          if (!encounteredNonsuspendableBreakpoint) insertCommand(new DumpSourceLocationCommand(this));
        }
        else if (handleStdResponse(line, iterator)) {
        }
        else if (line.startsWith(RESOLVED_BREAKPOINT_MARKER)) { // TODO: move to break point handler
          // Resolved breakpoint 1 to aaa() at A.mxml:14
          final String breakPointNumber =
            line.substring(RESOLVED_BREAKPOINT_MARKER.length(), line.indexOf(' ', RESOLVED_BREAKPOINT_MARKER.length()));
          myBreakpointsHandler.updateBreakpointStatusToVerified(breakPointNumber);
        }
        else if (line.startsWith(ATTEMPTING_TO_RESOLVE_BREAKPOINT_MARKER)) {  // TODO: move to break point handler
          int breakpointId = Integer.parseInt(line.substring(ATTEMPTING_TO_RESOLVE_BREAKPOINT_MARKER.length(), line.indexOf(',')));
          final XLineBreakpoint<XBreakpointProperties> breakpoint = myBreakpointsHandler.getBreakpointByIndex(breakpointId);

          if (iterator.hasNext() && iterator.getNext().indexOf("no executable code") != -1) {
            iterator.next();

            myBreakpointsHandler.updateBreakpointStatusToInvalid(breakpoint);
            toInsertContinue = true;
          }
          else if (iterator.hasNext() && iterator.getNext().indexOf(AMBIGUOUS_MATCHING_FILE_NAMES) != -1) {
            iterator.next();
            iterator.next();

            while (iterator.hasNext() && iterator.getNext().indexOf("#") != -1) {
              iterator.next();
            }

            if (getFileId(breakpoint.getSourcePosition().getFile().getPath()) != null) {
              final XBreakpointHandler handler = getBreakpointHandlers()[0];
              handler.unregisterBreakpoint(breakpoint, false);
              handler.registerBreakpoint(breakpoint);
            }

            toInsertContinue = true;
          }
        }
        else if (line.startsWith("Set additional breakpoints")) {
          //Set additional breakpoints as desired, and then type 'continue'.
          toInsertContinue = true;    // TODO: move to break point handler
        }
        else if (line.startsWith("Player session terminated")) {
          handleProbablyUnexpectedStop(line);
        }
        else if (line.indexOf("Execution halted") != -1) {
          if (!getSession().isPaused()) {
            getSession().pause();
          }
        }
      }

      if (toInsertContinue) insertCommand(new ContinueCommand());
    }
    while (explicitlyContinueRead || reader.hasSomeDataPending());
  }

  String defaultReadCommand(DebuggerCommand command) throws IOException {
    return reader.readLine(command.getEndVMState() == VMState.RUNNING);
  }

  private boolean handleStdResponse(String line, ResponseLineIterator iterator) {
    if (line.startsWith(TRACE_MARKER)) {
      myConsoleView.print(line + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
      return true;
    }
    else if (line.startsWith(FAULT_MARKER)) {
      myConsoleView.print(line + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
      while (iterator.hasNext() && iterator.getNext().startsWith("at ")) {
        myConsoleView.print(iterator.next() + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
      }
      return true;
    }
    else if (line.startsWith("[SWF]") || line.startsWith("[UnloadSWF]")) {
      if (!FilterSwfLoadUnloadMessagesAction.isFilterEnabled(getSession().getProject())) {
        myConsoleView.print(line + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
      }
      return true;
    }
    return false;
  }

  protected static <K, T> void addToMap(final Map<K, Collection<T>> map, final K key, final T valueCollectionElement) {
    Collection<T> valueCollection = map.get(key);
    if (valueCollection == null) {
      valueCollection = new ArrayList<T>(1);
      map.put(key, valueCollection);
    }

    if (!valueCollection.contains(valueCollectionElement)) {
      valueCollection.add(valueCollectionElement);
    }
  }

  protected void ensureFilePathToIdMapIsUpToDate() {
    if (myFileIdIsUpToDate) return; // this calculation is VERY costly
    sendAndProcessOneCommand(
      new DebuggerCommand("show files", CommandOutputProcessingType.SPECIAL_PROCESSING, VMState.SUSPENDED, VMState.SUSPENDED) {
        @Override
        CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
          processShowFilesResult(new StringTokenizer(s, "\r\n"));
          return CommandOutputProcessingMode.DONE;
        }
      }, null);
    myFileIdIsUpToDate = true;
  }

  protected void processShowFilesResult(StringTokenizer tokenizer) {
    while (tokenizer.hasMoreTokens()) {
      String line = tokenizer.nextToken();
      int spaceIndex = line.indexOf(' ');
      int commaPos = line.indexOf(',');

      if (spaceIndex == -1 || commaPos == -1) {
        log("Unexpected string format:" + line);
        continue;
      }

      String id = line.substring(0, spaceIndex);
      String fullPath = line.substring(spaceIndex + 1, commaPos).replace(File.separatorChar, '/');

      int markerIndex = fullPath.indexOf("/frameworks/projects/");
      if (markerIndex != -1 && fullPath.indexOf("/src/", markerIndex) > 0) {
        fullPath = mySdkLocation + fullPath.substring(markerIndex);
      }

      String shortName = line.substring(commaPos + 2);
      myFilePathToIdMap.put(fullPath, id);
      addToMap(myFileNameToPathsMap, shortName, fullPath);
    }
  }

  protected String resolveFileReference(VirtualFile file) {
    String marker;
    String id = myFilePathToIdMap.get(file.getPresentableUrl().replace(File.separatorChar, '/'));

    if (id != null) {
      marker = "#" + id;
    }
    else {
      marker = file.getName();
      String expectedPackageNameFromFile = JSResolveUtil.getExpectedPackageNameFromFile(file, getSession().getProject());
      if (!StringUtil.isEmpty(expectedPackageNameFromFile)) marker = expectedPackageNameFromFile + "." + marker;
    }
    return marker;
  }

  protected String getFileId(final String filePath) {
    ensureFilePathToIdMapIsUpToDate();
    return myFilePathToIdMap.get(filePath);
  }

  /**
   * Looks for file with specified <code>fileName</code> in caches or anywhere in the project in following order:
   * <ul>
   * <li>[1] matching <code>id</code></li>
   * <li>[2] in <code>myFileNameToFilesMap</code> matching <code>packageName</code></li>
   * <li>[3] in <code>myFileNameToPathsMap</code> matching <code>packageName</code></li>
   * <li>[4] in the whole project with libraries matching <code>packageName</code></li>
   * <li>[5] in <code>myFileNameToPathsMap</code></li>
   * <li>[6] in the whole project with libraries</li>
   * </ul>
   *
   * @param fileName
   * @param packageName used as auxiliary information if there are more than one file with the same name.
   *                    <code>null</code> means that we don't have information about package
   * @param id
   */
  @Nullable
  VirtualFile findFileByNameOrId(@NotNull String fileName, @Nullable String packageName, @Nullable String id) {
    // [1]
    if (id != null) {
      ensureFilePathToIdMapIsUpToDate();
      List<String> value = myFilePathToIdMap.getKeysByValue(id);
      if (value != null && value.size() > 0) {
        VirtualFile file = doFindFileByPath(value.get(0));
        if (file != null) return file;
        log("Cannot find file " + fileName + " by id " + id);
      }
    }

    return findFile(fileName, packageName);
  }

  /**
   * @see #findFileByNameOrId(String, String, String)
   */
  @Nullable
  private VirtualFile findFile(final String shortName, @Nullable String packageName) {
    if ("<null>".equals(shortName)) return null;

    if (packageName == null) {
      return findFile(shortName);
    }

    final String packagePath = packageName.replace(".", "/");

    // [2]
    final Collection<VirtualFile> cachedFiles = myFileNameToFilesMap.get(shortName);
    if (cachedFiles != null && !cachedFiles.isEmpty()) {
      for (final VirtualFile cachedFile : cachedFiles) {
        if (cachedFile != null) {
          final String path = cachedFile.getPath();
          int lastSlashIndex = path.lastIndexOf("/");
          final String folderPath = lastSlashIndex > 0 ? path.substring(0, lastSlashIndex) : "";
          if (folderPath.endsWith(packagePath)) {
            return cachedFile;
          }
        }
      }
    }

    // [3]
    final Collection<String> paths = myFileNameToPathsMap.get(shortName);
    if (paths != null && !paths.isEmpty()) {
      for (final String path : paths) {
        int lastSlashIndex = path.lastIndexOf("/");
        final String folderPath = lastSlashIndex > 0 ? path.substring(0, lastSlashIndex) : "";
        if (folderPath.endsWith(packagePath)) {
          final VirtualFile file = doFindFileByPath(path);
          if (file != null) {
            addToMap(myFileNameToFilesMap, shortName, file);
            return file;
          }
          break;
        }
      }
    }

    // [4]
    final Collection<VirtualFile> files =
      ApplicationManager.getApplication().runReadAction(new NullableComputable<Collection<VirtualFile>>() {
        public Collection<VirtualFile> compute() {
          final Project project = getSession().getProject();
          return FilenameIndex.getVirtualFilesByName(project, shortName, GlobalSearchScope.allScope(project));
        }
      });

    if (files != null && !files.isEmpty()) {
      for (final VirtualFile file : files) {
        if (file != null) {
          final String path = file.getPath();
          int lastSlashIndex = path.lastIndexOf("/");
          final String folderPath = lastSlashIndex > 0 ? path.substring(0, lastSlashIndex) : "";
          if (folderPath.endsWith(packagePath)) {
            addToMap(myFileNameToFilesMap, shortName, file);
            return file;
          }
        }
      }
    }

    return findFile(shortName);
  }

  /**
   * @see #findFileByNameOrId(String, String, String)
   */
  @Nullable
  private VirtualFile findFile(final String shortName) {
    // [5]
    final Collection<String> paths = myFileNameToPathsMap.get(shortName);
    if (paths != null && !paths.isEmpty()) {
      final VirtualFile file = doFindFileByPath(paths.iterator().next());
      if (file != null) {
        addToMap(myFileNameToFilesMap, shortName, file);
        return file;
      }
    }

    // [6]
    final Collection<VirtualFile> files =
      ApplicationManager.getApplication().runReadAction(new NullableComputable<Collection<VirtualFile>>() {
        public Collection<VirtualFile> compute() {
          final Project project = getSession().getProject();
          return FilenameIndex.getVirtualFilesByName(project, shortName, GlobalSearchScope.allScope(project));
        }
      });

    if (files != null && !files.isEmpty()) {
      final VirtualFile file = files.iterator().next();
      addToMap(myFileNameToFilesMap, shortName, file);
      return file;
    }

    return null;
  }

  @Nullable
  private static VirtualFile doFindFileByPath(final String path) {
    VirtualFile file;
    file = ApplicationManager.getApplication().runReadAction(new NullableComputable<VirtualFile>() {
      public VirtualFile compute() {
        return VfsUtil.findRelativeFile(path, null);
      }
    });
    return file;
  }

  private void handleProbablyUnexpectedStop(final String s) {
    if (!getSession().isStopped()) {
      log(s);
      if (myCheckForUnexpectedStartupStop) {
        myConsoleView.print(s + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
        if (!ourReportedAboutPossibleStartupFailure) {
          ourReportedAboutPossibleStartupFailure = true;
          reportProblem(s + "\n" + FlexBundle.message("flex.debugger.unexpected.stop"));
        }
      }
      getProcessHandler().detachProcess();
    }
  }

  private DebuggerCommand postCommand() throws InterruptedException, IOException {
    DebuggerCommand command = commandsToWrite.removeFirst();
    final boolean currentlyExecuting = !suspended && startupDone;

    if (command.getStartVMState() == VMState.RUNNING) {
      if (!currentlyExecuting) {
        if (command instanceof SuspendDebuggerCommand) ((SuspendDebuggerCommand)command).doCommandAfterSuspend();
        if (command.getOutputProcessingMode() != CommandOutputProcessingType.DEFAULT_PROCESSING) return null;
      }
      command.post(this);
      return command;
    }
    else if (!currentlyExecuting) {
      if (command.getEndVMState() == VMState.RUNNING) {
        final DebuggerCommand nextCommand = commandsToWrite.peek();
        if (nextCommand != null && nextCommand.getStartVMState() == VMState.SUSPENDED) {
          command = commandsToWrite.removeFirst();
          if (nextCommand.getEndVMState() == VMState.SUSPENDED && !(nextCommand instanceof QuitCommand)) {
            insertCommand(new ContinueCommand());
          }
        }
      }
    }

    if (currentlyExecuting) {
      command = new SuspendResumeDebuggerCommand(command);
    }

    command.post(this);
    return command;
  }

  boolean isDebuggerFromSdk4() {
    return myDebuggerVersion.startsWith("4.");
  }

  boolean isDebuggerFromSdk3() {
    return myDebuggerVersion.startsWith("3.");
  }

  void doSendCommandText(final DebuggerCommand command) throws IOException {
    final String text = command.getText();

    setSuspended(
      command.getOutputProcessingMode() != CommandOutputProcessingType.NO_PROCESSING
      ? false
      : command.getEndVMState() == VMState.SUSPENDED);
    log("Sent:" + text);
    fdbProcess.getOutputStream().write((text + "\n").getBytes());
    try {
      fdbProcess.getOutputStream().flush();
    }
    catch (IOException ex) {
      handleProbablyUnexpectedStop(FlexBundle.message("flex.debugger.unexpected.communication.error"));
    }
  }

  protected static void log(@NonNls String s) {
    s = System.currentTimeMillis() + " " + s;
    if (doSimpleTracing) System.out.println(s);
    if (LOG.isDebugEnabled()) LOG.debug(s);
  }

  static void log(final Throwable ex) {
    if (doSimpleTracing) ex.printStackTrace();
    LOG.error(ex);
  }

  public void startStepOver() {
    sendCommand(new DebuggerCommand("next"));
  }

  public void startStepInto() {
    sendCommand(new DebuggerCommand("step"));
  }

  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointsHandler.getBreakpointHandlers();
  }

  public void sessionInitialized() {
    super.sessionInitialized();

    sendCommand(new ContinueCommand() {
      @Override
      public CommandOutputProcessingType getOutputProcessingMode() {
        myCheckForUnexpectedStartupStop = true;
        return super.getOutputProcessingMode();
      }
    });

    myConsoleView = (ConsoleView)getSession().getRunContentDescriptor().getExecutionConsole();
    if (myFdbLaunchCommand != null) {
      myConsoleView.print(myFdbLaunchCommand + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    myOutputAlarm = new Alarm(Alarm.ThreadToUse.SHARED_THREAD, myConsoleView);

    scheduleOutputReading();
    scheduleFdbErrorStreamReading();
    getSession().setPauseActionSupported(true);

    synchronized (this) {
      debugSessionInitialized = true;
      notifyAll();
    }
  }

  private void scheduleOutputReading() {
    Runnable action = new Runnable() {
      public void run() {
        try {
          if (reader.hasSomeDataPending()) {
            sendCommand(new DumpOutputCommand());
          }
          else if (!myOutputAlarm.isDisposed()) {
            myOutputAlarm.addRequest(this, 100);
          }
        }
        catch (IOException ex) {
          myOutputAlarm.cancelAllRequests();
        }
      }
    };
    myOutputAlarm.addRequest(action, 500);
  }

  void addPendingCommand(final DebuggerCommand command, int delay) {
    myOutputAlarm.addRequest(new Runnable() {
      public void run() {
        sendCommand(command);
      }
    }, delay);
  }

  private void scheduleFdbErrorStreamReading() {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        InputStreamReader myErrorStreamReader = new InputStreamReader(fdbProcess.getErrorStream());
        try {
          char[] buf = new char[1024];
          int read;
          while ((read = myErrorStreamReader.read(buf, 0, buf.length)) >= 0) {
            String message = new String(buf, 0, read);
            LOG.debug("[fdb error stream]: " + message);
            myConsoleView.print(message, ConsoleViewContentType.ERROR_OUTPUT);
          }
        }
        catch (IOException e) {
          LOG.debug("fdb error stream reading error", e);
        }
        finally {
          try {
            myErrorStreamReader.close();
          }
          catch (IOException e) {/*ignore*/}
        }
      }
    });
  }

  public void startPausing() {
    sendCommand(new SuspendDebuggerCommand(new DumpSourceLocationCommand(this)));
  }

  public void startStepOut() {
    sendCommand(new DebuggerCommand("finish"));
  }

  public void stop() {
    if (myFlexUnitConnection != null) {
      myFlexUnitConnection.close();
    }
    if (myPolicyFileConnection != null) {
      myPolicyFileConnection.close();
    }


    sendCommand(new QuitCommand());

    if (adlProcess != null) {
      adlProcess.destroy();
    }

    if (!startupDone) {
      fdbProcess.destroy(); // connect will block input and quit command will not do the thing, but it needed for graceful cleanup
    }
  }

  private void reportProblem(final String s) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        Notifications.Bus
          .notify(new Notification(DEBUGGER_GROUP_ID, FlexBundle.message("flex.debugger.startup.error"), s.replace("\n", "<br>"),
                                   NotificationType.ERROR), getSession().getProject());
      }
    });
  }

  void insertCommand(DebuggerCommand command) {
    commandsToWrite.addFirst(command);
  }

  void sendCommand(DebuggerCommand command) {
    commandsToWrite.addLast(command);
  }

  public void resume() {
    sendCommand(new ContinueCommand());
  }

  public void runToPosition(@NotNull final XSourcePosition position) {
    myBreakpointsHandler.handleRunToPosition(position, this);
  }

  public void sendAndProcessOneCommand(final DebuggerCommand command, final @Nullable Function<Exception, Void> onException) {
    insertCommand(command);
    try {
      processOneCommandLoop();
    }
    catch (Exception e) {
      log(e);
      if (onException != null) {
        onException.fun(e);
      }
      else {
        throw new RuntimeException(e);
      }
    }
  }

  private void setSuspended(final boolean suspended) {
    this.suspended = suspended;
  }

  boolean filterStdResponse(String line) {
    ResponseLineIterator iterator = new ResponseLineIterator(line);
    boolean stdcontent = true;
    while (iterator.hasNext()) {
      String s = iterator.next();
      if (s.startsWith("$")) {  // $1 = 111
        stdcontent = false;
        break;
      }
      if (s.length() > 0 && Character.isDigit(s.charAt(0))) {
        sendCommand(new DumpSourceLocationCommand(this));
      }
      else if (!handleStdResponse(line, iterator)) {
        stdcontent = false;
      }
    }

    if (stdcontent) return true;
    return false;
  }

  void setQName2Id(Map<String, String> qName2IdMap, Object equalityObject) {
    myStackFrameEqualityObject = equalityObject;
    myQName2IdMap = qName2IdMap;
  }

  @Nullable
  Map<String, String> getQName2IdIfSameEqualityObject(Object equalityObject) {
    if (equalityObject != null && equalityObject.equals(myStackFrameEqualityObject)) return myQName2IdMap;
    return null;
  }

  class MyFdbOutputReader {
    private final InputStreamReader myReader;
    private final char[] buf = new char[8192];
    private final StringBuilder lastText = new StringBuilder();
    private int lastTextMarkerScanningStart;
    private final InputStream myInputStream;

    public MyFdbOutputReader(final InputStream _inputStream) {
      myReader = new InputStreamReader(_inputStream);
      myInputStream = _inputStream;
    }

    boolean hasSomeDataPending() throws IOException {
      return myInputStream.available() > 0;
    }

    String readLine(boolean nonblock) throws IOException {
      if (lastText != null) {
        final String lastText = getNextLine(nonblock);
        if (lastText != null) return lastText;
      }

      while (true) {
        int read = myReader.read(buf, 0, buf.length);
        if (read == -1) return null;
        lastText.append(buf, 0, read);

        if (read < buf.length) {
          final String lastText = getNextLine(nonblock);
          if (lastText != null) return lastText;
        }
      }
    }

    private String getNextLine(boolean allowEmptyMarker) {
      String result;
      String marker = FDB_MARKER;
      int i = lastText.indexOf(marker, lastTextMarkerScanningStart);

      if (i == -1) {
        marker = "(y or n)";
        i = lastText.indexOf(marker, lastTextMarkerScanningStart);
      }

      if (i == -1 &&
          (allowEmptyMarker || lastText.indexOf(WAITING_PLAYER_MARKER, lastTextMarkerScanningStart) >= 0) &&
          lastText.length() > 0) {
        i = lastText.length();
        marker = "";
      }

      if (i != -1) {
        result = lastText.substring(0, i);
        lastText.delete(0, i + marker.length());
        lastTextMarkerScanningStart = 0;
        if (isBlank(lastText)) lastText.setLength(0);
        setSuspended(marker.length() != 0);
        return result;
      }
      else {
        lastTextMarkerScanningStart = lastText.length();
        result = null;
      }
      return result;
    }

    private boolean isBlank(StringBuilder lastText) {
      for (int i = 0; i < lastText.length(); ++i) {
        if (lastText.charAt(i) != ' ') return false;
      }
      return true;
    }
  }

  public XValueMarkerProvider<FlexValue, String> createValueMarkerProvider() {
    return new XValueMarkerProvider<FlexValue, String>(FlexValue.class) {
      public boolean canMark(@NotNull final FlexValue value) {
        return getObjectId(value) != null;
      }

      public String getMarker(@NotNull final FlexValue value) {
        return getObjectId(value);
      }

      private String getObjectId(final FlexValue value) {
        final String text = value.getResult();
        final String prefix = "[Object ";
        final String suffix = FlexStackFrame.CLASS_MARKER;
        int suffixIndex;
        if (text.startsWith(prefix) && (suffixIndex = text.indexOf(suffix, prefix.length())) > 0) {
          try {
            return FlexStackFrame.validObjectId(text.substring(prefix.length(), suffixIndex));
          }
          catch (NumberFormatException e) {/*ignore*/}
        }
        return null;
      }
    };
  }

  static class QuitCommand extends DebuggerCommand {
    QuitCommand() {
      super("quit\ny", CommandOutputProcessingType.SPECIAL_PROCESSING);
    }
  }

  static class ContinueCommand extends DebuggerCommand {
    ContinueCommand() {
      super("continue", CommandOutputProcessingType.NO_PROCESSING, VMState.SUSPENDED, VMState.RUNNING);
    }
  }

  class ReadGreetingCommand extends DebuggerCommand {
    ReadGreetingCommand() {
      super("does not matter because post() is empty", CommandOutputProcessingType.SPECIAL_PROCESSING);
    }

    public String read(final FlexDebugProcess flexDebugProcess) throws IOException {
      return reader.readLine(true);
    }

    public void post(FlexDebugProcess flexDebugProcess) throws IOException {
    }

    CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
      myConsoleView.print(s + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
      return CommandOutputProcessingMode.DONE;
    }
  }

  class StartAirAppDebuggingCommand extends StartDebuggingCommand {
    private final GeneralCommandLine myAdlCommandLine;
    private final String[] myAirLaunchCommand;
    private final @Nullable VirtualFile myTempDirToDeleteWhenProcessFinished;

    StartAirAppDebuggingCommand(final String[] airLaunchCommand, final @Nullable VirtualFile tempDirToDeleteWhenProcessFinished) {
      myAdlCommandLine = null;
      myAirLaunchCommand = airLaunchCommand;
      myTempDirToDeleteWhenProcessFinished = tempDirToDeleteWhenProcessFinished;
    }

    public StartAirAppDebuggingCommand(final GeneralCommandLine adlCommandLine) {
      myAdlCommandLine = adlCommandLine;
      myAirLaunchCommand = null;
      myTempDirToDeleteWhenProcessFinished = null;
    }

    void launchDebuggedApplication() throws IOException {
      launchAdl();
    }

    private void launchAdl() throws IOException {
      if (myAdlCommandLine != null) {
        try {
          myConsoleView.print(myAdlCommandLine.getCommandLineString() + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
          adlProcess = myAdlCommandLine.createProcess();
        }
        catch (ExecutionException e) {
          throw new IOException(e.getMessage());
        }
      }
      else {
        final Function<String, String> quoter = new Function<String, String>() {
          public String fun(final String s) {
            return s.contains(" ") ? "\"" + s + "\"" : s;
          }
        };
        myConsoleView.print(ADL_PREFIX + StringUtil.join(myAirLaunchCommand, quoter, " ") + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
        adlProcess = Runtime.getRuntime().exec(myAirLaunchCommand);
      }

      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        public void run() {
          InputStreamReader reader = new InputStreamReader(adlProcess.getInputStream());
          try {
            char[] buf = new char[1024];
            int read;
            while ((read = reader.read(buf, 0, buf.length)) >= 0) {
              String message = new String(buf, 0, read);
              LOG.debug("[adl input stream]: " + message);
              if (!startupDone) {
                myConsoleView.print(ADL_PREFIX + message + (message.endsWith("\n") ? "" : "\n"), ConsoleViewContentType.ERROR_OUTPUT);
              }
            }
            // the process is likely already destroyed because input stream is finished, though double check makes no harm
            adlProcess.destroy();
            try {
              int exitCode = adlProcess.exitValue();
              myConsoleView.print(ADL_PREFIX + IdeBundle.message("finished.with.exit.code.text.message", exitCode) + "\n",
                                  exitCode == 0 ? ConsoleViewContentType.SYSTEM_OUTPUT : ConsoleViewContentType.ERROR_OUTPUT);
            }
            catch (IllegalThreadStateException ignore) {
            }
            // need to destroy fdb process that is waiting for player to start
            getProcessHandler().detachProcess();
          }
          catch (IOException e) {
            LOG.debug("adl input stream reading error", e);
            myConsoleView.print(ADL_PREFIX + e.getMessage() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
          }
          finally {
            if (myTempDirToDeleteWhenProcessFinished != null) {
              FlexUtils.removeFileLater(myTempDirToDeleteWhenProcessFinished);
            }
            try {
              reader.close();
            }
            catch (IOException e) {/*ignore*/}
          }
        }
      });

      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        public void run() {
          InputStreamReader reader = new InputStreamReader(adlProcess.getErrorStream());
          try {
            char[] buf = new char[1024];
            int read;
            while ((read = reader.read(buf, 0, buf.length)) >= 0) {
              String message = new String(buf, 0, read);
              LOG.debug("[adl error stream]: " + message);
            }
          }
          catch (IOException e) {
            LOG.debug("adl error stream reading error", e);
          }
          finally {
            try {
              reader.close();
            }
            catch (IOException e) {/*ignore*/}
          }
        }
      });
    }
  }

  class StartAppOnAndroidDeviceCommand extends StartDebuggingCommand {

    private final Sdk myFlexSdk;
    private final AirMobileRunnerParameters myRunnerParameters;
    private final String myAppId;

    StartAppOnAndroidDeviceCommand(final Sdk flexSdk, final String appId) {
      myFlexSdk = flexSdk;
      myAppId = appId;
      myRunnerParameters = null;
    }

    public StartAppOnAndroidDeviceCommand(final Sdk flexSdk, final AirMobileRunnerParameters runnerParameters) {
      myFlexSdk = flexSdk;
      myRunnerParameters = runnerParameters;
      myAppId = null;
    }

    void launchDebuggedApplication() throws IOException {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          if (myRunnerParameters != null) {
            FlexBaseRunner.launchOnDevice(getSession().getProject(), myFlexSdk, myRunnerParameters, true);
          }
          else {
            FlexBaseRunner.launchOnAndroidDevice(getSession().getProject(), myFlexSdk, myAppId, true);
          }
        }
      });
    }
  }

  class LaunchBrowserCommand extends StartDebuggingCommand {
    private final @NotNull String myUrl;
    private final LauncherParameters myLauncherParameters;

    LaunchBrowserCommand(final @NotNull String url, final LauncherParameters launcherParameters) {
      myUrl = url;
      myLauncherParameters = launcherParameters;
    }

    void launchDebuggedApplication() {
      FlexBaseRunner.launchWithSelectedApplication(myUrl, myLauncherParameters);
    }
  }

  protected void notifyFdbWaitingForPlayerStateReached() {
    if (connectToRunningFlashPlayerMode) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          final ToolWindowManager manager = ToolWindowManager.getInstance(getSession().getProject());

          final HyperlinkAdapter h = new HyperlinkAdapter() {
            protected void hyperlinkActivated(final HyperlinkEvent e) {
              manager.notifyByBalloon(ToolWindowId.DEBUG, MessageType.INFO, FlexBundle.message("remote.flash.debug.details",
                                                                                               getOwnIpAddress()));
            }
          };

          manager.notifyByBalloon(ToolWindowId.DEBUG, MessageType.INFO, FlexBundle.message("remote.flash.debugger.waiting"), null, h);
        }
      });
    }
  }

  private static String getOwnIpAddress() {
    try {
      final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        final Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          final InetAddress inetAddress = inetAddresses.nextElement();
          if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
            return inetAddress.getHostAddress();
          }
        }
      }
    }
    catch (SocketException ignore) {/* ignore */}
    return "unknown";
  }

  class StartDebuggingCommand extends DebuggerCommand {

    StartDebuggingCommand() {
      super("run", CommandOutputProcessingType.SPECIAL_PROCESSING);
      setSuspended(true);
    }

    @Override
    CommandOutputProcessingMode onTextAvailable(String s) {
      StringBuilder builder = new StringBuilder(s.length());
      StringTokenizer tokenizer = new StringTokenizer(s, "\r\n");

      while (tokenizer.hasMoreTokens()) {
        String next = tokenizer.nextToken();
        if (next.indexOf("type 'continue'") == -1) {
          builder.append(next + "\n");
        }
      }

      s = builder.toString();

      final ResponseLineIterator iterator = new ResponseLineIterator(s);
      while (iterator.hasNext()) {
        final String line = iterator.next();
        if (line.startsWith("[SWF]") || line.startsWith("[UnloadSWF]")) {
          if (!FilterSwfLoadUnloadMessagesAction.isFilterEnabled(getSession().getProject())) {
            myConsoleView.print(line + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
          }
        }
        else {
          myConsoleView.print(line + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
        }
      }

      if (s.indexOf("Another Flash debugger is probably running") != -1) {
        reportProblem(s);
        getProcessHandler().detachProcess();
        return CommandOutputProcessingMode.DONE;
      }
      if (s.indexOf("Failed to connect") != -1) {
        reportProblem(s);
        handleProbablyUnexpectedStop(s);
        return CommandOutputProcessingMode.DONE;
      }

      if (s.indexOf(WAITING_PLAYER_MARKER) != -1) {
        fdbWaitingForPlayerStateReached = true;
        getSession().rebuildViews();
        notifyFdbWaitingForPlayerStateReached();

        try {
          launchDebuggedApplication();
        }
        catch (IOException e) {
          reportProblem(s);
          handleProbablyUnexpectedStop(s);
          return CommandOutputProcessingMode.DONE;
        }
      }
      else {
        startupDone = (s.indexOf("Player connected; session starting.") != -1);
        if (startupDone) {
          if (connectToRunningFlashPlayerMode) {
            final Balloon balloon = ToolWindowManager.getInstance(getSession().getProject()).getToolWindowBalloon(ToolWindowId.DEBUG);
            if (balloon != null) {
              balloon.hide();
            }
          }

          getSession().rebuildViews();
          return CommandOutputProcessingMode.DONE;
        }
      }

      return CommandOutputProcessingMode.PROCEEDING;
    }

    void launchDebuggedApplication() throws IOException {
    }
  }

  private class SuspendResumeDebuggerCommand extends SuspendDebuggerCommand {

    public SuspendResumeDebuggerCommand(final DebuggerCommand command1) {
      super(command1);
    }

    @Override
    protected void doCommandAfterSuspend() {
      if (!(myCommand1 instanceof QuitCommand)) insertCommand(new ContinueCommand());
      super.doCommandAfterSuspend();
    }
  }

  private class SuspendDebuggerCommand extends DebuggerCommand {
    protected final DebuggerCommand myCommand1;

    public SuspendDebuggerCommand(final DebuggerCommand command1) {
      super("suspend", CommandOutputProcessingType.SPECIAL_PROCESSING, VMState.RUNNING, VMState.SUSPENDED);
      myCommand1 = command1;
    }

    @Override
    CommandOutputProcessingMode onTextAvailable(@NonNls final String s) {
      insertCommand(new DebuggerCommand("y", CommandOutputProcessingType.SPECIAL_PROCESSING) {
        @Override
        CommandOutputProcessingMode onTextAvailable(@NonNls final String s) {
          doCommandAfterSuspend();
          return CommandOutputProcessingMode.DONE;
        }
      });

      return CommandOutputProcessingMode.DONE;
    }

    protected void doCommandAfterSuspend() {
      insertCommand(myCommand1);
    }
  }

  private class DumpOutputCommand extends DebuggerCommand {
    DumpOutputCommand() {
      super("dump", CommandOutputProcessingType.DEFAULT_PROCESSING, VMState.RUNNING, VMState.RUNNING);
    }

    @Override
    public void post(final FlexDebugProcess flexDebugProcess) throws IOException {
      scheduleOutputReading();
    }
  }

  @Override
  public XSmartStepIntoHandler<?> getSmartStepIntoHandler() {
    return new FlexSmartStepIntoHandler(this);
  }

  @Override
  public void registerAdditionalActions(@NotNull final DefaultActionGroup leftToolbar, @NotNull final DefaultActionGroup topToolbar) {
    topToolbar.addAction(ActionManager.getInstance().getAction("Flex.Debugger.FilterSwfLoadUnloadMessages"));
  }
}
