// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.ide.IdeCoreBundle;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageUtil;
import com.intellij.lang.javascript.flex.actions.airpackage.DeviceInfo;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitConnection;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.flexunit.SwfPolicyFileConnection;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
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
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.Function;
import com.intellij.util.PathUtil;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.frame.XValueMarkerProvider;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.LocalFileFinder;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileDebugTransport;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileRunTarget;
import static com.intellij.lang.javascript.flex.run.RemoteFlashRunnerParameters.RemoteDebugTarget;

/**
 * @author Maxim.Mossienko
 */
public class FlexDebugProcess extends XDebugProcess {
  private static final String TRACE_MARKER = "[trace] ";
  public static final String DEBUGGER_GROUP_ID = "Debugger";
  private static final String SRC_PATH_ELEMENT = "/src/";

  private boolean debugSessionInitialized;
  private final Process fdbProcess;
  private Process adlProcess;

  private final MyFdbOutputReader reader;
  private Alarm myOutputAlarm;

  private final Module myModule;
  private final FlexBuildConfiguration myBC;
  private final BCBasedRunnerParameters myRunnerParameters;

  private final String myAppSdkHome;
  private final String myDebuggerSdkHome;
  private final String myDebuggerVersion;

  @NonNls static final String RESOLVED_BREAKPOINT_MARKER = "Resolved breakpoint ";
  @NonNls static final String BREAKPOINT_MARKER = "Breakpoint ";
  @NonNls private static final String FDB_MARKER = "(fdb) ";
  @NonNls private static final String WAITING_PLAYER_MARKER_1 = "Waiting for Player to connect";
  @NonNls private static final String WAITING_PLAYER_MARKER_2 = "Trying to connect to Player";
  @NonNls static final String ATTEMPTING_TO_RESOLVE_BREAKPOINT_MARKER = "Attempting to resolve breakpoint ";

  @NonNls private static final String ADL_PREFIX = "[AIR Debug Launcher]: ";

  private boolean myCheckForUnexpectedStartupStop;
  private Thread myDebuggerManagerThread;
  @NonNls static final String AMBIGUOUS_MATCHING_FILE_NAMES = "Ambiguous matching file names:";

  private final FlexBreakpointsHandler myBreakpointsHandler;
  @NonNls private static final String FAULT_MARKER = "[Fault] ";
  private static final Logger LOG = Logger.getInstance(FlexDebugProcess.class.getName());
  private static final boolean doSimpleTracing = ApplicationManager.getApplication().isInternal();

  private Object myStackFrameEqualityObject;
  private Map<String, String> myQName2IdMap;

  private int myCurrentWorker = 0;
  private final KnownFilesInfo myKnownFilesInfo = new KnownFilesInfo(this);

  private String myFdbLaunchCommand;

  private final LinkedList<DebuggerCommand> commandsToWrite = new LinkedList<>() {
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
  private boolean startupDone;
  private ConsoleView myConsoleView;
  private FlexUnitConnection myFlexUnitConnection;
  private SwfPolicyFileConnection myPolicyFileConnection;

  public FlexDebugProcess(final XDebugSession session,
                          final FlexBuildConfiguration bc,
                          final BCBasedRunnerParameters params) throws IOException {
    super(session);
    myModule = ModuleManager.getInstance(session.getProject()).findModuleByName(params.getModuleName());
    myBC = bc;
    myRunnerParameters = params;

    LOG.assertTrue(myModule != null);

    final Sdk sdk = bc.getSdk();
    LOG.assertTrue(sdk != null);
    myAppSdkHome = FileUtil.toSystemIndependentName(sdk.getHomePath());

    final Sdk sdkForDebugger = params instanceof FlashRunnerParameters && bc.getTargetPlatform() == TargetPlatform.Web
                               ? getDebuggerSdk(((FlashRunnerParameters)params).getDebuggerSdkRaw(), sdk)
                               : sdk;
    myDebuggerSdkHome = FileUtil.toSystemIndependentName(sdkForDebugger.getHomePath());
    myDebuggerVersion = sdkForDebugger.getVersionString();
    myBreakpointsHandler = new FlexBreakpointsHandler(this);

    final List<String> fdbLaunchCommand = FlexSdkUtils
      .getCommandLineForSdkTool(session.getProject(), sdkForDebugger, getFdbClasspath(), "flex.tools.debugger.cli.DebugCLI", null);

    if (isFlexSdk_4_12plus_IdeMode()) {
      fdbLaunchCommand.add("-ide");
    }

    if (params instanceof FlashRunnerParameters &&
        bc.getTargetPlatform() == TargetPlatform.Mobile &&
        (((FlashRunnerParameters)params).getMobileRunTarget() == AirMobileRunTarget.AndroidDevice ||
         ((FlashRunnerParameters)params).getMobileRunTarget() == AirMobileRunTarget.iOSDevice) &&
        ((FlashRunnerParameters)params).getDebugTransport() == AirMobileDebugTransport.USB) {
      fdbLaunchCommand.add("-p");
      fdbLaunchCommand.add(String.valueOf(((FlashRunnerParameters)params).getUsbDebugPort()));
    }

    if (params instanceof RemoteFlashRunnerParameters &&
        (((RemoteFlashRunnerParameters)params).getRemoteDebugTarget() == RemoteDebugTarget.AndroidDevice ||
         ((RemoteFlashRunnerParameters)params).getRemoteDebugTarget() == RemoteDebugTarget.iOSDevice) &&
        ((RemoteFlashRunnerParameters)params).getDebugTransport() == AirMobileDebugTransport.USB) {
      fdbLaunchCommand.add("-p");
      fdbLaunchCommand.add(String.valueOf(((RemoteFlashRunnerParameters)params).getUsbDebugPort()));
    }

    fdbProcess = launchFdb(fdbLaunchCommand);

    if (params instanceof FlashRunnerParameters) {
      final FlashRunnerParameters appParams = (FlashRunnerParameters)params;

      switch (bc.getTargetPlatform()) {
        case Web -> {
          final String urlOrPath = appParams.isLaunchUrl() ? appParams.getUrl()
                                                           : bc.isUseHtmlWrapper()
                                                             ? PathUtil.getParentPath(bc.getActualOutputFilePath()) +
                                                               "/" + BCUtils.getWrapperFileName(bc)
                                                             : bc.getActualOutputFilePath();
          sendCommand(new LaunchBrowserCommand(urlOrPath, appParams.getLauncherParameters()));
        }
        case Desktop -> sendAdlStartingCommand(bc, appParams);
        case Mobile -> {
          switch (appParams.getMobileRunTarget()) {
            case Emulator -> sendAdlStartingCommand(bc, appParams);
            case AndroidDevice -> {
              final String androidAppId =
                FlexBaseRunner.getApplicationId(FlexBaseRunner.getAirDescriptorPath(bc, bc.getAndroidPackagingOptions()));
              sendCommand(new StartAppOnAndroidDeviceCommand(bc.getSdk(), appParams.getDeviceInfo(), androidAppId));
            }
            case iOSSimulator -> {
              final String iosSimulatorAppId =
                FlexBaseRunner.getApplicationId(FlexBaseRunner.getAirDescriptorPath(bc, bc.getIosPackagingOptions()));
              sendCommand(new StartAppOnIosSimulatorCommand(bc.getSdk(), iosSimulatorAppId,
                                                            ((FlashRunnerParameters)params).getIOSSimulatorSdkPath(),
                                                            ((FlashRunnerParameters)params).getIOSSimulatorDevice()));
            }
            case iOSDevice -> {
              final String iosAppName =
                FlexBaseRunner.getApplicationName(FlexBaseRunner.getAirDescriptorPath(bc, bc.getIosPackagingOptions()));
              sendCommand(new StartAppOnIosDeviceCommand(iosAppName));
            }
          }
        }
      }
    }
    else if (params instanceof FlexUnitRunnerParameters) {
      final FlexUnitRunnerParameters flexUnitParams = (FlexUnitRunnerParameters)params;
      openFlexUnitConnections(flexUnitParams.getSocketPolicyPort(), flexUnitParams.getPort());
      if (bc.getTargetPlatform() == TargetPlatform.Web) {
        sendCommand(new LaunchBrowserCommand(bc.getActualOutputFilePath(), flexUnitParams.getLauncherParameters()));
      }
      else {
        sendAdlStartingCommand(bc, params);
      }
    }
    else {
      // Flash Remote Debug run configuration
      sendCommand(new StartDebuggingCommand());
    }

    reader = new MyFdbOutputReader(fdbProcess.getInputStream());

    startCommandProcessingThread();
  }

  @Nullable
  public Module getModule() {
    return myModule.isDisposed() ? null : myModule;
  }

  public FlexBuildConfiguration getBC() {
    return myBC;
  }

  public boolean isFlexUnit() {
    return myRunnerParameters instanceof FlexUnitRunnerParameters;
  }

  public String getAppSdkHome() {
    return myAppSdkHome;
  }

  public static Sdk getDebuggerSdk(final String sdkRaw, final Sdk bcSdk) {
    if (sdkRaw.equals(FlexSdkComboBoxWithBrowseButton.BC_SDK_KEY)) {
      return bcSdk;
    }
    else {
      final Sdk sdk = FlexSdkUtils.findFlexOrFlexmojosSdk(sdkRaw);
      LOG.assertTrue(sdk != null);
      return sdk;
    }
  }

  private String getFdbClasspath() {
    final String legacyFdbPath = myDebuggerSdkHome + "/lib/legacy/fdb.jar";
    if (new File(legacyFdbPath).isFile()) {
      return legacyFdbPath;
    }

    String classpath = myDebuggerSdkHome + "/lib/fdb.jar";

    if (isDebuggerFromSdk3()) {
      classpath = FlexCommonUtils.getPathToBundledJar("idea-fdb-3-fix.jar") + File.pathSeparator + classpath;
    }
    else if (!myDebuggerVersion.startsWith(FlexCommonUtils.AIR_SDK_VERSION_PREFIX)) {
      if (StringUtil.compareVersionNumbers(myDebuggerVersion, "4.0") >= 0 &&
          StringUtil.compareVersionNumbers(myDebuggerVersion, "4.1.1") < 0) {
        classpath = FlexCommonUtils.getPathToBundledJar("idea-fdb-4.0.0.14159-fix.jar") + File.pathSeparator + classpath;
      }
      else if (myDebuggerVersion.startsWith("4.6.b")
               ||
               (StringUtil.compareVersionNumbers(myDebuggerVersion, "4.5") >= 0 &&
                StringUtil.compareVersionNumbers(myDebuggerVersion, "4.6.1") < 0)
               ||
               (StringUtil.compareVersionNumbers(myDebuggerVersion, "4.8") >= 0 &&
                StringUtil.compareVersionNumbers(myDebuggerVersion, "4.12") < 0)) {
        classpath = FlexCommonUtils.getPathToBundledJar("idea-fdb-4.5.0.20967-fix.jar") + File.pathSeparator + classpath;
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
        @Override
        public void statusChanged(FlexUnitConnection.ConnectionStatus status) {
          if (status == FlexUnitConnection.ConnectionStatus.CONNECTION_FAILED) {
            getSession().stop();
          }
        }

        @Override
        public void onData(String line) {
          getProcessHandler().notifyTextAvailable(line + "\n", ProcessOutputTypes.STDOUT);
        }

        @Override
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
    myFdbLaunchCommand = StringUtil.join(fdbLaunchCommand,
                                         s -> s.indexOf(' ') >= 0 && !(s.startsWith("\"") && s.endsWith("\"")) ? '\"' + s + '\"' : s, " ");

    final Process process = Runtime.getRuntime().exec(ArrayUtilRt.toStringArray(fdbLaunchCommand));
    sendCommand(new ReadGreetingCommand()); // just to read copyrights and wait for "(fdb)"
    return process;
  }

  private void startCommandProcessingThread() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      myDebuggerManagerThread = Thread.currentThread();
      synchronized (this) {
        if (!debugSessionInitialized) {
          try {
            this.wait();
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
    });
  }

  private void sendAdlStartingCommand(final FlexBuildConfiguration bc, final BCBasedRunnerParameters params) throws IOException {
    try {
      final Sdk sdk = bc.getSdk();
      LOG.assertTrue(sdk != null);

      final boolean needToRemoveAirRuntimeDir;
      final VirtualFile airRuntimeDirForFlexmojosSdk;

      if (sdk.getSdkType() instanceof FlexmojosSdkType) {
        final Pair<VirtualFile, Boolean> airRuntimeDirInfo;
        airRuntimeDirInfo = FlexSdkUtils.getAirRuntimeDirInfoForFlexmojosSdk(sdk);
        needToRemoveAirRuntimeDir = airRuntimeDirInfo.second;
        airRuntimeDirForFlexmojosSdk = airRuntimeDirInfo.first;
      }
      else {
        needToRemoveAirRuntimeDir = false;
        airRuntimeDirForFlexmojosSdk = null;
      }

      final String airRuntimePath = airRuntimeDirForFlexmojosSdk == null ? null : airRuntimeDirForFlexmojosSdk.getPath();
      sendCommand(
        new StartAirAppDebuggingCommand(FlexBaseRunner.createAdlCommandLine(getSession().getProject(), params, bc, airRuntimePath),
                                        needToRemoveAirRuntimeDir ? airRuntimeDirForFlexmojosSdk : null));
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

  private static final Set<String> ourAlreadyMadeExecutable = new HashSet<>();

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

      if (commandOutput.contains("Player session terminated") && !(command instanceof SuspendResumeDebuggerCommand)) {
        handleProbablyUnexpectedStop(commandOutput);
        break;
      }

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

        if (line.startsWith("Active worker has changed to worker ")) {
          try {
            final String workerText = line.substring("Active worker has changed to worker ".length());
            if ("Main Thread".equals(workerText)) {
              myCurrentWorker = 0;
            }
            else {
              myCurrentWorker = Integer.parseInt(workerText);
            }
          }
          catch (NumberFormatException e) {
            log("Unexpected worker number");
          }
        }
        else if (line.contains("Additional ActionScript code has been loaded")) {
          if (!suspended) reader.readLine(false);
          myKnownFilesInfo.setUpToDate(false);
        }
        else if ((index = line.indexOf(BREAKPOINT_MARKER)) != -1 && !line.contains(" created")) { // TODO: move to break point handler
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
                FlexStackFrame frame = new FlexStackFrame(this, breakpoint.getSourcePosition());
                boolean suspend = false;
                if (evaluateCondition(breakpoint.getConditionExpression(), frame)) {
                  String message = evaluateMessage(breakpoint.getLogExpressionObject(), frame);
                  suspend = getSession().breakpointReached(breakpoint, message, new FlexSuspendContext(frame));
                }
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

          if (iterator.hasNext() && iterator.getNext().contains("no executable code")) {
            iterator.next();

            myBreakpointsHandler.updateBreakpointStatusToInvalid(breakpoint);
            toInsertContinue = true;
          }
          else if (iterator.hasNext() && iterator.getNext().contains(AMBIGUOUS_MATCHING_FILE_NAMES)) {
            iterator.next();
            iterator.next();

            while (iterator.hasNext() && iterator.getNext().contains("#")) {
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
        else if (line.contains("Execution halted")) {
          if (!getSession().isPaused()) {
            getSession().pause();
          }
        }
      }

      if (toInsertContinue) insertCommand(new ContinueCommand());
    }
    while (explicitlyContinueRead || reader.hasSomeDataPending());
  }

  private boolean evaluateCondition(@Nullable XExpression expression, FlexStackFrame frame) {
    if (expression == null || StringUtil.isEmptyOrSpaces(expression.getExpression())) {
      return true;
    }

    final String result = frame.eval(expression.getExpression(), this);

    if (result != null && (result.equalsIgnoreCase("true") || result.equalsIgnoreCase("false"))) {
      return Boolean.parseBoolean(result);
    }
    else {
      final String message = result == null || result.startsWith(FlexStackFrame.CANNOT_EVALUATE_EXPRESSION)
                             ? FlexBundle.message("failed.to.evaluate.breakpoint.condition", expression)
                             : FlexBundle.message("not.boolean.breakpoint.condition", expression, result);
      final Ref<Boolean> stopRef = new Ref<>(false);

      ApplicationManager.getApplication().invokeAndWait(() -> {
        final Project project = getSession().getProject();
        final int answer =
          Messages.showYesNoDialog(project, message, FlexBundle.message("breakpoint.condition.error"), Messages.getQuestionIcon());
        stopRef.set(answer == Messages.YES);
      });

      return stopRef.get();
    }
  }

  @Nullable
  private String evaluateMessage(@Nullable XExpression expression, FlexStackFrame frame) {
    if (expression == null || StringUtil.isEmptyOrSpaces(expression.getExpression())) {
      return null;
    }
    return frame.eval(expression.getExpression(), this);
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

  protected String resolveFileReference(VirtualFile file) {
    String marker;
    String id = myKnownFilesInfo.getIdByFilePathNoUpdate(file.getPath());

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
    return myKnownFilesInfo.getIdByFilePath(filePath);
  }

  /**
   * Looks for file with specified {@code fileName} in caches or anywhere in the project in following order:
   * <ul>
   * <li>[1] matching {@code id}. If the file is not within the project - try to find if its copy exists within the project</li>
   * <li>[2] in {@code myFileNameToPathsMap} matching {@code packageName}. If the file is not within the project - try to find if its copy exists within the project</li>
   * <li>[3] in the whole project with libraries matching {@code packageName} (prefer in BC scope)</li>
   * <li>[4] in the whole project with libraries (prefer BC scope)</li>
   * </ul>
   *
   * @param packageName used as auxiliary information if there are more than one file with the same name.
   *                    {@code null} means that we don't have information about package
   */
  @Nullable
  VirtualFile findFileByNameOrId(final @NotNull String fileName, @Nullable String packageName, final @Nullable String id) {
    // [1]
    if (id != null) {
      final String path = myKnownFilesInfo.getFilePathById(myCurrentWorker, id);

      if (path != null) {
        final VirtualFile fileById = LocalFileFinder.findFile(path);

        if (packageName == null) {
          // try to guess package name
          final String mavenStyleSrc = "/src/main/flex/";
          int srcIndex = path.indexOf(mavenStyleSrc);
          if (srcIndex > 0) {
            packageName = StringUtil.getPackageName(path.substring(srcIndex + mavenStyleSrc.length()), '/').replace('/', '.');
          }
          else {
            srcIndex = path.indexOf(SRC_PATH_ELEMENT);
            if (srcIndex > 0) {
              packageName = StringUtil.getPackageName(path.substring(srcIndex + SRC_PATH_ELEMENT.length()), '/').replace('/', '.');
            }
          }
        }

        if (fileById != null) {
          return getThisOrSimilarFileInProject(fileById, packageName);
        }
      }
    }

    if ("<null>".equals(fileName)) return null;

    return packageName == null ? findFile(fileName) : findFile(fileName, packageName);
  }

  /**
   * @see #findFileByNameOrId(String, String, String)
   */
  @Nullable
  private VirtualFile findFile(final String fileName, @NotNull String packageName) {
    final String packagePath = packageName.replace('.', '/');

    // [2]
    final Collection<String> paths = myKnownFilesInfo.getPathsByName(myCurrentWorker, fileName);

    if (paths != null) {
      for (final String path : paths) {
        final String folderPath = PathUtil.getParentPath(path);
        if (folderPath.endsWith(packagePath)) {
          final VirtualFile file = LocalFileFinder.findFile(path);
          if (file != null) {
            return getThisOrSimilarFileInProject(file, packageName);
          }
        }
      }
    }

    // [3]
    final GlobalSearchScope bcScopeBase = FlexUtils.getModuleWithDependenciesAndLibrariesScope(myModule, myBC, isFlexUnit());
    final GlobalSearchScope bcScope = uniteWithLibrarySourcesOfBC(bcScopeBase, myModule, myBC, new HashSet<>());

    Collection<VirtualFile> files = getFilesByName(getSession().getProject(), bcScope, fileName);

    VirtualFile file = getFileMatchingPackageName(getSession().getProject(), files, packageName);
    if (file == null) {
      files = getFilesByName(getSession().getProject(), GlobalSearchScope.allScope(getSession().getProject()), fileName);
      file = getFileMatchingPackageName(getSession().getProject(), files, packageName);
    }

    return file != null ? file : findFile(fileName);
  }

  /**
   * @see #findFileByNameOrId(String, String, String)
   */
  @Nullable
  private VirtualFile findFile(final String fileName) {
    // [4]
    final GlobalSearchScope bcScope = FlexUtils.getModuleWithDependenciesAndLibrariesScope(myModule, myBC, isFlexUnit());
    Collection<VirtualFile> files = getFilesByName(getSession().getProject(), bcScope, fileName);

    if (files.isEmpty()) {
      files = getFilesByName(getSession().getProject(), GlobalSearchScope.allScope(getSession().getProject()), fileName);
    }

    if (!files.isEmpty()) {
      return files.iterator().next();
    }

    // last chance to find file out of project
    final Collection<String> paths = myKnownFilesInfo.getPathsByName(myCurrentWorker, fileName);
    if (paths != null) {
      for (final String path : paths) {
        final VirtualFile file = LocalFileFinder.findFile(path);
        if (file != null) {
          return file;
        }
      }
    }

    return null;
  }

  @NotNull
  private VirtualFile getThisOrSimilarFileInProject(final @NotNull VirtualFile file, final String packageName) {
    final Project project = getSession().getProject();
    final GlobalSearchScope allScope = GlobalSearchScope.allScope(project);

    if (allScope.contains(file)) {
      return file;
    }

    // File is found on the computer but it doesn't belong to the project. That means that the library is compiled on this computer,
    // but in this project it is configured as if it were a 3rd party library. So we'll try to find if sources of this library are also configured.

    final Collection<VirtualFile> files = getFilesByName(project, allScope, file.getName());

    if (packageName == null) {
      return !files.isEmpty() ? files.iterator().next() : file;
    }
    else {
      final VirtualFile fileMatchingPackage = getFileMatchingPackageName(project, files, packageName);
      return fileMatchingPackage != null ? fileMatchingPackage : file;
    }
  }

  private static Collection<VirtualFile> getFilesByName(final Project project, final GlobalSearchScope scope, final String fileName) {
    return ReadAction.compute(() -> FilenameIndex.getVirtualFilesByName(fileName, scope));
  }

  @Nullable
  private static VirtualFile getFileMatchingPackageName(final Project project,
                                                        final Collection<VirtualFile> files,
                                                        final String packageName) {
    for (VirtualFile file : files) {
      final VirtualFile sourceRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(file);
      final String relPath = sourceRoot == null ? null : VfsUtilCore.getRelativePath(file, sourceRoot, '/');
      final String packagePath = relPath == null ? null : PathUtil.getParentPath(relPath).replace('/', '.');
      if (packagePath != null && packagePath.equals(packageName)) {
        return file;
      }
    }
    return null;
  }

  private static GlobalSearchScope uniteWithLibrarySourcesOfBC(GlobalSearchScope scope,
                                                               final Module module,
                                                               final FlexBuildConfiguration bc,
                                                               final Collection<FlexBuildConfiguration> processedConfigurations) {
    if (!processedConfigurations.add(bc)) return scope;

    final Collection<VirtualFile> libSourceRoots = new HashSet<>();

    final Sdk sdk = bc.getSdk();
    if (sdk != null) {
      Collections.addAll(libSourceRoots, sdk.getRootProvider().getFiles(OrderRootType.SOURCES));
    }

    for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        final Module otherModule = ((BuildConfigurationEntry)entry).findModule();
        final FlexBuildConfiguration otherBC = ((BuildConfigurationEntry)entry).findBuildConfiguration();
        if (otherModule != null && otherBC != null) {
          scope = uniteWithLibrarySourcesOfBC(scope, otherModule, otherBC, processedConfigurations);
        }
      }
      else if (entry instanceof ModuleLibraryEntry) {
        final LibraryOrderEntry orderEntry =
          FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, ModuleRootManager.getInstance(module));
        if (orderEntry != null) {
          Collections.addAll(libSourceRoots, orderEntry.getFiles(OrderRootType.SOURCES));
        }
      }
      else if (entry instanceof SharedLibraryEntry) {
        final Library library = FlexProjectRootsUtil.findOrderEntry(module.getProject(), (SharedLibraryEntry)entry);
        if (library != null) {
          Collections.addAll(libSourceRoots, library.getFiles(OrderRootType.SOURCES));
        }
      }
    }

    return libSourceRoots.isEmpty() ? scope
                                    : scope.uniteWith(new LibrarySourcesSearchScope(module.getProject(), libSourceRoots));
  }

  private void handleProbablyUnexpectedStop(final String s) {
    if (!getSession().isStopped()) {
      log(s);
      if (myCheckForUnexpectedStartupStop) {
        myConsoleView.print(s + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
      }
      getProcessHandler().detachProcess();
    }
  }

  private DebuggerCommand postCommand() throws IOException {
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

  boolean isDebuggerFromSdk3() {
    return myDebuggerVersion != null && myDebuggerVersion.startsWith("3.");
  }

  boolean isFlexSdk_4_12plus_IdeMode() {
    return !myDebuggerVersion.startsWith(FlexCommonUtils.AIR_SDK_VERSION_PREFIX) &&
           StringUtil.compareVersionNumbers(myDebuggerVersion, "4.12") > 0;
  }

  void doSendCommandText(final DebuggerCommand command) throws IOException {
    final String text = command.getText();

    setSuspended(
      command.getOutputProcessingMode() == CommandOutputProcessingType.NO_PROCESSING && command.getEndVMState() == VMState.SUSPENDED);
    log("Sent:" + text);
    fdbProcess.getOutputStream().write((text + "\n").getBytes(StandardCharsets.UTF_8));
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

  @Override
  public void startStepOver(@Nullable XSuspendContext context) {
    sendCommand(new DebuggerCommand("next"));
  }

  @Override
  public void startStepInto(@Nullable XSuspendContext context) {
    sendCommand(new DebuggerCommand("step"));
  }

  @Override
  public XBreakpointHandler<?> @NotNull [] getBreakpointHandlers() {
    return myBreakpointsHandler.getBreakpointHandlers();
  }

  @Override
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

    myOutputAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, myConsoleView);

    scheduleOutputReading();
    scheduleFdbErrorStreamReading();
    getSession().setPauseActionSupported(true);

    synchronized (this) {
      debugSessionInitialized = true;
      notifyAll();
    }
  }

  private void scheduleOutputReading() {
    if (myOutputAlarm.isDisposed()) return;

    Runnable action = new Runnable() {
      @Override
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
    myOutputAlarm.addRequest(action, 0);
  }

  void addPendingCommand(final DebuggerCommand command, int delay) {
    myOutputAlarm.addRequest(() -> sendCommand(command), delay);
  }

  private void scheduleFdbErrorStreamReading() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try (InputStreamReader myErrorStreamReader = new InputStreamReader(fdbProcess.getErrorStream(), StandardCharsets.UTF_8)) {
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
    });
  }

  @Override
  public void startPausing() {
    sendCommand(new SuspendDebuggerCommand(new DumpSourceLocationCommand(this)));
  }

  @Override
  public void startStepOut(@Nullable XSuspendContext context) {
    sendCommand(new DebuggerCommand("finish"));
  }

  @Override
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
    Notifications.Bus
      .notify(new Notification(DEBUGGER_GROUP_ID, FlexBundle.message("flex.debugger.startup.error"), s.replace("\n", "<br>"),
                               NotificationType.ERROR), getSession().getProject());
  }

  void insertCommand(DebuggerCommand command) {
    commandsToWrite.addFirst(command);
  }

  void sendCommand(DebuggerCommand command) {
    commandsToWrite.addLast(command);
  }

  @Override
  public void resume(@Nullable XSuspendContext context) {
    sendCommand(new ContinueCommand());
  }

  @Override
  public void runToPosition(@NotNull final XSourcePosition position, @Nullable XSuspendContext context) {
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

    MyFdbOutputReader(final InputStream _inputStream) {
      myReader = FlexCommonUtils.createInputStreamReader(_inputStream);
      myInputStream = _inputStream;
    }

    boolean hasSomeDataPending() throws IOException {
      return myInputStream.available() > 0;
    }

    String readLine(boolean nonblock) throws IOException {
      {
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
          (allowEmptyMarker ||
           lastText.indexOf(WAITING_PLAYER_MARKER_1, lastTextMarkerScanningStart) >= 0 ||
           lastText.indexOf(WAITING_PLAYER_MARKER_2, lastTextMarkerScanningStart) >= 0) &&
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

  @Override
  public XValueMarkerProvider<FlexValue, String> createValueMarkerProvider() {
    return new XValueMarkerProvider<>(FlexValue.class) {
      @Override
      public boolean canMark(@NotNull final FlexValue value) {
        return getObjectId(value) != null;
      }

      @Override
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

    @Override
    public String read(final FlexDebugProcess flexDebugProcess) throws IOException {
      return reader.readLine(true);
    }

    @Override
    public void post(FlexDebugProcess flexDebugProcess) throws IOException {
    }

    @Override
    CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
      myConsoleView.print(s + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
      return CommandOutputProcessingMode.DONE;
    }
  }

  class StartAirAppDebuggingCommand extends StartDebuggingCommand {
    private final GeneralCommandLine myAdlCommandLine;
    private final @Nullable VirtualFile myTempDirToDeleteWhenProcessFinished;

    StartAirAppDebuggingCommand(final GeneralCommandLine adlCommandLine,
                                final @Nullable VirtualFile tempDirToDeleteWhenProcessFinished) {
      myAdlCommandLine = adlCommandLine;
      myTempDirToDeleteWhenProcessFinished = tempDirToDeleteWhenProcessFinished;
    }

    @Override
    void launchDebuggedApplication() throws IOException {
      launchAdl();
    }

    private void launchAdl() throws IOException {
      try {
        myConsoleView.print(myAdlCommandLine.getCommandLineString() + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
        adlProcess = myAdlCommandLine.createProcess();
      }
      catch (ExecutionException e) {
        throw new IOException(e.getMessage());
      }

      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        try (InputStreamReader reader12 = new InputStreamReader(adlProcess.getInputStream(), StandardCharsets.UTF_8)) {
          char[] buf = new char[1024];
          int read;
          while ((read = reader12.read(buf, 0, buf.length)) >= 0) {
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
            myConsoleView.print(ADL_PREFIX + IdeCoreBundle.message("finished.with.exit.code.text.message", exitCode) + "\n",
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
        }
      });

      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        try (InputStreamReader reader1 = new InputStreamReader(adlProcess.getErrorStream(), StandardCharsets.UTF_8)) {
          char[] buf = new char[1024];
          int read;
          while ((read = reader1.read(buf, 0, buf.length)) >= 0) {
            String message = new String(buf, 0, read);
            LOG.debug("[adl error stream]: " + message);
          }
        }
        catch (IOException e) {
          LOG.debug("adl error stream reading error", e);
        }
      });
    }
  }

  class StartAppOnAndroidDeviceCommand extends StartDebuggingCommand {

    private final Sdk myFlexSdk;
    private final @Nullable DeviceInfo myDevice;
    private final String myAppId;

    StartAppOnAndroidDeviceCommand(final Sdk flexSdk, final @Nullable DeviceInfo device, final String appId) {
      myFlexSdk = flexSdk;
      myDevice = device;
      myAppId = appId;
    }

    @Override
    void launchDebuggedApplication() {
      ApplicationManager.getApplication().invokeLater(
        () -> FlexBaseRunner.launchOnAndroidDevice(getSession().getProject(), myFlexSdk, myDevice, myAppId, true));
    }
  }

  class StartAppOnIosSimulatorCommand extends StartDebuggingCommand {

    private final Sdk myFlexSdk;
    private final String myAppId;
    private final String myIOSSdkPath;
    private final String mySimulatorDevice;

    StartAppOnIosSimulatorCommand(final Sdk flexSdk, final String appId, final String iOSSdkPath, String simulatorDevice) {
      myFlexSdk = flexSdk;
      myAppId = appId;
      myIOSSdkPath = iOSSdkPath;
      mySimulatorDevice = simulatorDevice;
    }

    @Override
    void launchDebuggedApplication() {
      ApplicationManager.getApplication().invokeLater(
        () -> FlexBaseRunner.launchOnIosSimulator(getSession().getProject(), myFlexSdk, myAppId, myIOSSdkPath, mySimulatorDevice, true));
    }
  }

  class StartAppOnIosDeviceCommand extends StartDebuggingCommand {
    private final String myAppName;

    StartAppOnIosDeviceCommand(final String appName) {
      myAppName = appName;
    }

    @Override
    void launchDebuggedApplication() {
      ApplicationManager.getApplication().invokeLater(() -> {
        final String adtVersion = AirPackageUtil.getAdtVersion(myModule.getProject(), myBC.getSdk());
        if (StringUtil.compareVersionNumbers(adtVersion, "3.4") >= 0) {
          final String message = FlexBundle.message("ios.application.installed.to.debug", myAppName);
          ToolWindowManager.getInstance(myModule.getProject()).notifyByBalloon(ToolWindowId.DEBUG, MessageType.INFO, message);
        }
        else {
          final String ipaName = myBC.getIosPackagingOptions().getPackageFileName() + ".ipa";
          final String outputFolder = PathUtil.getParentPath(myBC.getActualOutputFilePath());

          final String message = FlexBundle.message("ios.application.packaged.to.debug", ipaName);
          ToolWindowManager.getInstance(myModule.getProject())
            .notifyByBalloon(ToolWindowId.DEBUG, MessageType.INFO, message, null, new HyperlinkAdapter() {
              @Override
              protected void hyperlinkActivated(final @NotNull HyperlinkEvent e) {
                RevealFileAction.openFile(new File(outputFolder + "/" + ipaName));
              }
            });
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

    @Override
    void launchDebuggedApplication() {
      FlexBaseRunner.launchWithSelectedApplication(myUrl, myLauncherParameters);
    }
  }

  protected void notifyFdbWaitingForPlayerStateReached() {
    if (myRunnerParameters instanceof RemoteFlashRunnerParameters) {
      final RemoteDebugTarget remoteDebugTarget = ((RemoteFlashRunnerParameters)myRunnerParameters).getRemoteDebugTarget();
      final AirMobileDebugTransport mobileDebugTransport = ((RemoteFlashRunnerParameters)myRunnerParameters).getDebugTransport();
      final int usbDebugPort = ((RemoteFlashRunnerParameters)myRunnerParameters).getUsbDebugPort();

      final String message;

      if (remoteDebugTarget == RemoteDebugTarget.Computer) {
        message = FlexBundle.message("remote.flash.debug.computer", FlexUtils.getOwnIpAddress());
      }
      else {
        final String device = remoteDebugTarget == RemoteDebugTarget.AndroidDevice ? "Android" : "iOS";
        message = mobileDebugTransport == AirMobileDebugTransport.Network
                  ? FlexBundle.message("remote.flash.debug.mobile.network", device, FlexUtils.getOwnIpAddress())
                  : FlexBundle.message("remote.flash.debug.mobile.usb", device, String.valueOf(usbDebugPort));
      }
      ApplicationManager.getApplication().invokeLater(
        () -> ToolWindowManager.getInstance(getSession().getProject()).notifyByBalloon(ToolWindowId.DEBUG, MessageType.INFO, message));
    }
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
        if (!next.contains("type 'continue'")) {
          builder.append(next).append("\n");
        }
      }

      s = builder.toString();

      final int unexpectedVersionIndex = s.indexOf("Unexpected version of the Flash Player");
      if (unexpectedVersionIndex >= 0) {
        s = s.substring(0, unexpectedVersionIndex) + "Session timed out or u" + s.substring(unexpectedVersionIndex + 1);
      }

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

      if (s.contains("Another Flash debugger is probably running")) {
        reportProblem(s);
        getProcessHandler().detachProcess();
        return CommandOutputProcessingMode.DONE;
      }

      if (s.contains("Failed to connect") || s.contains("unexpected version of the Flash Player") || s.contains("Connection refused")) {
        reportProblem(s);
        handleProbablyUnexpectedStop(s);
        return CommandOutputProcessingMode.DONE;
      }

      if (s.contains(WAITING_PLAYER_MARKER_1) || s.contains(WAITING_PLAYER_MARKER_2)) {
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
        startupDone = (s.contains("Player connected; session starting."));
        if (startupDone) {
          final Balloon balloon = ToolWindowManager.getInstance(getSession().getProject()).getToolWindowBalloon(ToolWindowId.DEBUG);
          if (balloon != null) {
            ApplicationManager.getApplication().invokeLater(() -> balloon.hide());
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

    SuspendResumeDebuggerCommand(final DebuggerCommand command1) {
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

    SuspendDebuggerCommand(final DebuggerCommand command1) {
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
  public void registerAdditionalActions(@NotNull final DefaultActionGroup leftToolbar,
                                        @NotNull final DefaultActionGroup topToolbar,
                                        @NotNull DefaultActionGroup settings) {
    topToolbar.addAction(ActionManager.getInstance().getAction("Flex.Debugger.FilterSwfLoadUnloadMessages"));
  }
}
