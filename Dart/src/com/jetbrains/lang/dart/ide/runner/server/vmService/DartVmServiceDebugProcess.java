// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.google.common.base.Charsets;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.TimeoutUtil;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.actions.DartPopFrameAction;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import com.jetbrains.lang.dart.ide.runner.server.OpenDartObservatoryUrlAction;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceEvaluator;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceSuspendContext;
import com.jetbrains.lang.dart.ide.runner.server.webdev.DartDaemonParserUtil;
import com.jetbrains.lang.dart.util.DartBazelFileUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TIntObjectHashMap;
import org.dartlang.vm.service.VmService;
import org.dartlang.vm.service.element.*;
import org.dartlang.vm.service.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DartVmServiceDebugProcess extends XDebugProcess {
  private static final Logger LOG = Logger.getInstance(DartVmServiceDebugProcess.class.getName());

  private static final String ORG_DARTLANG_APP_PREFIX = "org-dartlang-app://";

  private final @Nullable ExecutionResult myExecutionResult;
  private final @NotNull DartUrlResolver myDartUrlResolver;

  private boolean myVmConnected = false;

  private final XBreakpointHandler @NotNull [] myBreakpointHandlers;
  private final IsolatesInfo myIsolatesInfo;
  private VmServiceWrapper myVmServiceWrapper;

  private final @NotNull Set<String> mySuspendedIsolateIds = Collections.synchronizedSet(new THashSet<>());
  private String myLatestCurrentIsolateId;

  private final Map<String, LightVirtualFile> myScriptIdToContentMap = new THashMap<>();
  private final Map<String, TIntObjectHashMap<Pair<Integer, Integer>>> myScriptIdToLinesAndColumnsMap =
    new THashMap<>();

  private final @Nullable String myDASExecutionContextId;
  private final @NotNull DebugType myDebugType;
  private final int myTimeout;
  private final @Nullable VirtualFile myCurrentWorkingDirectory;
  protected @Nullable String myRemoteProjectRootUri;
  private @Nullable String myBazelWorkspacePath;

  private final @NotNull OpenDartObservatoryUrlAction myOpenObservatoryAction =
    new OpenDartObservatoryUrlAction(null, () -> myVmConnected && !getSession().isStopped());

  public enum DebugType {
    CLI, REMOTE, WEBDEV
  }

  /**
   * @deprecated use another constructor
   */
  @Deprecated
  public DartVmServiceDebugProcess(@NotNull XDebugSession session,
                                   @NotNull String debuggingHost,
                                   int observatoryPort,
                                   @Nullable ExecutionResult executionResult,
                                   @NotNull DartUrlResolver dartUrlResolver,
                                   @Nullable String dasExecutionContextId,
                                   @NotNull DebugType debugType,
                                   int timeout,
                                   @Nullable VirtualFile currentWorkingDirectory) {
    this(session, executionResult, dartUrlResolver, dasExecutionContextId, debugType, timeout, currentWorkingDirectory);
  }

  public DartVmServiceDebugProcess(@NotNull XDebugSession session,
                                   @Nullable ExecutionResult executionResult,
                                   @NotNull DartUrlResolver dartUrlResolver,
                                   @Nullable String dasExecutionContextId,
                                   @NotNull DebugType debugType,
                                   int timeout,
                                   @Nullable VirtualFile currentWorkingDirectory) {
    super(session);
    myExecutionResult = executionResult;
    myDartUrlResolver = dartUrlResolver;
    myDebugType = debugType;
    myTimeout = timeout;
    myCurrentWorkingDirectory = currentWorkingDirectory;

    myIsolatesInfo = new IsolatesInfo();

    myBreakpointHandlers = new XBreakpointHandler[]{
      new DartVmServiceBreakpointHandler(this),
      new DartExceptionBreakpointHandler(this)
    };

    myDASExecutionContextId = dasExecutionContextId;

    if (DebugType.REMOTE == debugType) {
      LOG.assertTrue(myExecutionResult == null && myDASExecutionContextId == null, myExecutionResult);
    }
    else if (DebugType.CLI == debugType) {
      LOG.assertTrue(myExecutionResult != null && myDASExecutionContextId != null, myDASExecutionContextId + myExecutionResult);
    }
    else if (DebugType.WEBDEV == debugType) {
      LOG.assertTrue(myExecutionResult != null && myDASExecutionContextId == null, myExecutionResult);
    }
  }

  public void start() throws ExecutionException {
    setLogger();

    getSession().addSessionListener(new XDebugSessionListener() {
      @Override
      public void sessionPaused() {
        stackFrameChanged();
      }

      @Override
      public void stackFrameChanged() {
        final XStackFrame stackFrame = getSession().getCurrentStackFrame();
        myLatestCurrentIsolateId =
          stackFrame instanceof DartVmServiceStackFrame ? ((DartVmServiceStackFrame)stackFrame).getIsolateId() : null;
      }
    });

    if (DebugType.REMOTE == myDebugType) {
      // Accepted inputs:
      // - http://127.0.0.1:<port>/
      // - http://127.0.0.1:<port>/<auth-code>
      // - https://127.0.0.1:<port>/<auth-code>
      // - ws://127.0.0.1:<port>/<auth-code>/ws
      // - vm@ws://127.0.0.1:<port>/<auth-code>/ws
      //
      // All of the above are then parsed/ morphed into the form "ws://127.0.0.1:<port>/<auth-code>/ws"
      String debugUrl = Messages.showInputDialog(getSession().getProject(),
                                                 DartBundle.message("enter.url.to.running.dart.app"),
                                                 DartBundle.message("connect.to.running.app.title"),
                                                 null,
                                                 "http://127.0.0.1:12345/AUTH_CODE=/",
                                                 new InputValidator() {
                                                   @Override
                                                   public boolean checkInput(String inputString) {
                                                     inputString = inputString.trim();
                                                     return inputString.startsWith("http://") ||
                                                            inputString.startsWith("https://") ||
                                                            inputString.startsWith("ws://") && inputString.endsWith("/ws") ||
                                                            inputString.startsWith("vm@ws://") && inputString.endsWith("/ws");
                                                   }

                                                   @Override
                                                   public boolean canClose(String inputString) {
                                                     return true;
                                                   }
                                                 }, null, DartBundle.message("connect.to.running.app.comment"));

      if (debugUrl == null) {
        // Cancel button pressed
        throw new ExecutionException(DartBundle.message("debugger.dialog.message.cancelled"));
      }

      debugUrl = debugUrl.trim();

      final String wsToConnect;
      if (debugUrl.startsWith("http://")) {
        // Convert the dialog entry of some "http://127.0.0.1:PORT/AUTH_CODE=" to "ws://127.0.0.1:PORT/AUTH_CODE=/ws"
        // The end of the URL may or may not have a slash, but multiple slashes won't be connected to:
        wsToConnect = "ws" + StringUtil.trimStart(debugUrl, "http") + (debugUrl.endsWith("/") ? "" : "/") + "ws";
      }
      else if (debugUrl.startsWith("https://")) {
        // Convert the dialog entry of some "https://127.0.0.1:PORT/AUTH_CODE=" to "ws://127.0.0.1:PORT/AUTH_CODE=/ws"
        // The end of the URL may or may not have a slash, but multiple slashes won't be connected to:
        wsToConnect = "ws" + StringUtil.trimStart(debugUrl, "https") + (debugUrl.endsWith("/") ? "" : "/") + "ws";
      }
      else if (debugUrl.startsWith("vm@ws://")) {
        // Convert the dialog entry of some "vm@ws://127.0.0.1:PORT/AUTH_CODE=/ws" to "ws://127.0.0.1:PORT/AUTH_CODE=/ws"
        // This is included as this is the format printed at the top of the Observatory page.
        wsToConnect = StringUtil.trimStart(debugUrl, "vm@");
      }
      else {
        // This is the debugUrl.startsWith("ws://") case,
        // don't modify the dialog entry if it appears like "ws://127.0.0.1:PORT/AUTH_CODE=/ws"
        wsToConnect = debugUrl;
      }

      scheduleConnect(wsToConnect);
      myOpenObservatoryAction.setUrl(debugUrl);
    }
    else if (DebugType.CLI == myDebugType) {
      getProcessHandler().addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
          final String prefix = DartConsoleFilter.OBSERVATORY_LISTENING_ON + "http://";
          if (event.getText().startsWith(prefix)) {
            getProcessHandler().removeProcessListener(this);

            final String urlBase = event.getText().substring(prefix.length());
            scheduleConnect("ws://" + StringUtil.trimTrailing(urlBase.trim(), '/') + "/ws");
            myOpenObservatoryAction.setUrl("http://" + urlBase);
          }
        }
      });
    }
    else if (DebugType.WEBDEV == myDebugType) {
      getProcessHandler().addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
          try {
            String wsUri = DartDaemonParserUtil.getWsUri(event.getText().trim());
            if (wsUri != null) {
              getProcessHandler().removeProcessListener(this);
              scheduleConnect(wsUri);
            }
          }
          catch (Exception e) {
            LOG.debug(e);
          }
        }
      });
    }
  }

  public ExceptionPauseMode getBreakOnExceptionMode() {
    return DartExceptionBreakpointHandler
      .getBreakOnExceptionMode(getSession(),
                               DartExceptionBreakpointHandler.getDefaultExceptionBreakpoint(getSession().getProject()));
  }

  public VmServiceWrapper getVmServiceWrapper() {
    return myVmServiceWrapper;
  }

  public Collection<IsolatesInfo.IsolateInfo> getIsolateInfos() {
    return myIsolatesInfo.getIsolateInfos();
  }

  private void setLogger() {
    Logging.setLogger(new org.dartlang.vm.service.logging.Logger() {
      @Override
      public void logError(final String message) {
        if (message.contains("\"code\":102,")) { // Cannot add breakpoint, already logged in logInformation()
          return;
        }

        if (message.contains("\"method\":\"removeBreakpoint\"")) { // That's expected because we set one breakpoint twice
          return;
        }

        getSession().getConsoleView().print("Error: " + message + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        LOG.error(message);
      }

      @Override
      public void logError(final String message, final Throwable exception) {
        getSession().getConsoleView().print("Error: " + message + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        LOG.error(message, exception);
      }

      @Override
      public void logInformation(String message) {
        if (message.length() > 500) {
          message = message.substring(0, 300) + "..." + message.substring(message.length() - 200);
        }
        LOG.debug(message);
      }

      @Override
      public void logInformation(final String message, final Throwable exception) {
        LOG.debug(message, exception);
      }
    });
  }

  protected void scheduleConnect(@NotNull String url) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      long startTime = System.currentTimeMillis();

      try {
        while (true) {
          try {
            connect(url);
            getSession().rebuildViews(); // to update status text to 'Connected'
            break;
          }
          catch (IOException e) {
            if (System.currentTimeMillis() > startTime + myTimeout) {
              throw e;
            }
            else {
              TimeoutUtil.sleep(50);
            }
          }
        }
      }
      catch (IOException e) {
        StringBuilder message = new StringBuilder("Failed to connect to the VM observatory service: " + e.toString() + "\n");
        Throwable cause = e.getCause();
        while (cause != null) {
          message.append("Caused by: ").append(cause.toString()).append("\n");
          final Throwable cause1 = cause.getCause();
          if (cause1 != cause) {
            cause = cause1;
          }
        }

        getSession().getConsoleView().print(message.toString(), ConsoleViewContentType.ERROR_OUTPUT);
        getSession().stop();
      }
    });
  }

  private void connect(@NotNull String url) throws IOException {
    final VmService vmService = VmService.connect(url);
    final DartVmServiceListener vmServiceListener =
      new DartVmServiceListener(this, (DartVmServiceBreakpointHandler)myBreakpointHandlers[0]);

    vmService.addVmServiceListener(vmServiceListener);

    myVmServiceWrapper =
      new VmServiceWrapper(this, vmService, vmServiceListener, myIsolatesInfo, (DartVmServiceBreakpointHandler)myBreakpointHandlers[0]);
    myVmServiceWrapper.handleDebuggerConnected();

    myVmConnected = true;
  }

  @Override
  protected ProcessHandler doGetProcessHandler() {
    return myExecutionResult == null ? super.doGetProcessHandler() : myExecutionResult.getProcessHandler();
  }

  @Override
  public @NotNull ExecutionConsole createConsole() {
    return myExecutionResult == null ? super.createConsole() : myExecutionResult.getExecutionConsole();
  }

  @Override
  public @NotNull XDebuggerEditorsProvider getEditorsProvider() {
    return new DartDebuggerEditorsProvider();
  }

  @Override
  public XBreakpointHandler<?> @NotNull [] getBreakpointHandlers() {
    return myBreakpointHandlers;
  }

  public boolean isRemoteDebug() {
    return DebugType.REMOTE == myDebugType;
  }

  public boolean isWebdevDebug() {
    return DebugType.WEBDEV == myDebugType;
  }

  public void guessRemoteProjectRoot(@NotNull ElementList<LibraryRef> libraries) {
    final VirtualFile pubspec = myDartUrlResolver.getPubspecYamlFile();
    final VirtualFile projectRoot = pubspec != null ? pubspec.getParent() : myCurrentWorkingDirectory;

    if (projectRoot == null) return;

    for (LibraryRef library : libraries) {
      final String remoteUri = library.getUri();
      if (remoteUri.startsWith(DartUrlResolver.DART_PREFIX)) continue;
      if (remoteUri.startsWith(DartUrlResolver.PACKAGE_PREFIX)) continue;

      final PsiFile[] localFilesWithSameName = ReadAction.compute(() -> {
        final String remoteFileName = PathUtil.getFileName(remoteUri);
        final GlobalSearchScope scope = GlobalSearchScopesCore.directoryScope(getSession().getProject(), projectRoot, true);
        return FilenameIndex.getFilesByName(getSession().getProject(), remoteFileName, scope);
      });

      int howManyFilesMatch = 0;

      for (PsiFile psiFile : localFilesWithSameName) {
        final VirtualFile file = DartResolveUtil.getRealVirtualFile(psiFile);
        if (file == null) continue;

        LOG.assertTrue(file.getPath().startsWith(projectRoot.getPath() + "/"), file.getPath() + "," + projectRoot.getPath());
        final String relPath = file.getPath().substring(projectRoot.getPath().length()); // starts with slash
        if (remoteUri.endsWith(relPath)) {
          howManyFilesMatch++;
          myRemoteProjectRootUri = remoteUri.substring(0, remoteUri.length() - relPath.length());
        }
      }

      if (howManyFilesMatch == 1) {
        break; // we did the best guess we could
      }
    }
  }

  @Override
  public void startStepOver(@Nullable XSuspendContext context) {
    if (myLatestCurrentIsolateId != null && mySuspendedIsolateIds.contains(myLatestCurrentIsolateId)) {
      DartVmServiceSuspendContext suspendContext = (DartVmServiceSuspendContext)context;
      final StepOption stepOption = suspendContext != null && suspendContext.getAtAsyncSuspension() ? StepOption.OverAsyncSuspension
                                                                                                    : StepOption.Over;
      myVmServiceWrapper.resumeIsolate(myLatestCurrentIsolateId, stepOption);
    }
  }

  @Override
  public void startStepInto(@Nullable XSuspendContext context) {
    if (myLatestCurrentIsolateId != null && mySuspendedIsolateIds.contains(myLatestCurrentIsolateId)) {
      myVmServiceWrapper.resumeIsolate(myLatestCurrentIsolateId, StepOption.Into);
    }
  }

  @Override
  public void startStepOut(@Nullable XSuspendContext context) {
    if (myLatestCurrentIsolateId != null && mySuspendedIsolateIds.contains(myLatestCurrentIsolateId)) {
      myVmServiceWrapper.resumeIsolate(myLatestCurrentIsolateId, StepOption.Out);
    }
  }

  public void dropFrame(DartVmServiceStackFrame frame) {
    myVmServiceWrapper.dropFrame(frame.getIsolateId(), frame.getFrameIndex() + 1);
  }

  @Override
  public void stop() {
    myVmConnected = false;

    if (myVmServiceWrapper != null) {
      if (myDASExecutionContextId != null) {
        DartAnalysisServerService.getInstance(getSession().getProject()).execution_deleteContext(myDASExecutionContextId);
      }

      Disposer.dispose(myVmServiceWrapper);
    }
  }

  @Override
  public void resume(@Nullable XSuspendContext context) {
    for (String isolateId : new ArrayList<>(mySuspendedIsolateIds)) {
      myVmServiceWrapper.resumeIsolate(isolateId, null);
    }
  }

  @Override
  public void startPausing() {
    for (IsolatesInfo.IsolateInfo info : getIsolateInfos()) {
      if (!mySuspendedIsolateIds.contains(info.getIsolateId())) {
        myVmServiceWrapper.pauseIsolate(info.getIsolateId());
      }
    }
  }

  @Override
  public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
    if (myLatestCurrentIsolateId != null && mySuspendedIsolateIds.contains(myLatestCurrentIsolateId)) {
      // Set a temporary breakpoint and resume.
      myVmServiceWrapper.addTemporaryBreakpoint(position, myLatestCurrentIsolateId);
      myVmServiceWrapper.resumeIsolate(myLatestCurrentIsolateId, null);
    }
  }

  public void isolateSuspended(@NotNull IsolateRef isolateRef) {
    mySuspendedIsolateIds.add(isolateRef.getId());
  }

  public boolean isIsolateSuspended(@NotNull String isolateId) {
    return mySuspendedIsolateIds.contains(isolateId);
  }

  public boolean isIsolateAlive(@NotNull String isolateId) {
    for (IsolatesInfo.IsolateInfo isolateInfo : myIsolatesInfo.getIsolateInfos()) {
      if (isolateId.equals(isolateInfo.getIsolateId())) {
        return true;
      }
    }
    return false;
  }

  public void isolateResumed(@NotNull IsolateRef isolateRef) {
    mySuspendedIsolateIds.remove(isolateRef.getId());
  }

  public void isolateExit(@NotNull IsolateRef isolateRef) {
    myIsolatesInfo.deleteIsolate(isolateRef);
    mySuspendedIsolateIds.remove(isolateRef.getId());

    if (isolateRef.getId().equals(myLatestCurrentIsolateId)) {
      resume(getSession().getSuspendContext()); // otherwise no way no resume them from UI
    }
  }

  public void handleWriteEvent(String base64Data) {
    String message = new String(Base64.getDecoder().decode(base64Data), Charsets.UTF_8);
    getSession().getConsoleView().print(message, ConsoleViewContentType.NORMAL_OUTPUT);
  }

  @Override
  public String getCurrentStateMessage() {
    return getSession().isStopped()
           ? XDebuggerBundle.message("debugger.state.message.disconnected")
           : myVmConnected
             ? XDebuggerBundle.message("debugger.state.message.connected")
             : DartBundle.message("debugger.trying.to.connect");
  }

  @Override
  public void registerAdditionalActions(@NotNull DefaultActionGroup leftToolbar,
                                        @NotNull DefaultActionGroup topToolbar,
                                        @NotNull DefaultActionGroup settings) {
    // For Run tool window this action is added in DartCommandLineRunningState.createActions()
    topToolbar.addSeparator();
    topToolbar.addAction(myOpenObservatoryAction);
    topToolbar.addAction(new DartPopFrameAction());
  }

  public @NotNull Collection<String> getUrisForFile(@NotNull VirtualFile file) {
    final Set<String> result = new HashSet<>();
    String uriByIde = myDartUrlResolver.getDartUrlForFile(file);

    // If dart:, short circuit the results.
    if (uriByIde.startsWith(DartUrlResolver.DART_PREFIX)) {
      result.add(uriByIde);
      return result;
    }

    final String filePath = file.getPath();

    // file:
    if (uriByIde.startsWith(DartUrlResolver.FILE_PREFIX)) {
      result.add(threeSlashize(uriByIde));
    }
    else {
      result.add(uriByIde);
      result.add(threeSlashize(new File(filePath).toURI().toString()));
    }

    // package: (if applicable)
    if (myDASExecutionContextId != null) {
      final String uriByServer =
        DartAnalysisServerService.getInstance(getSession().getProject()).execution_mapUri(myDASExecutionContextId, filePath, null);
      if (uriByServer != null) {
        result.add(uriByServer);
      }
    }

    // remote prefix (if applicable)
    if (myRemoteProjectRootUri != null) {
      final VirtualFile pubspec = myDartUrlResolver.getPubspecYamlFile();
      if (pubspec != null) {
        final String projectPath = pubspec.getParent().getPath();
        if (filePath.startsWith(projectPath)) {
          result.add(myRemoteProjectRootUri + filePath.substring(projectPath.length()));
        }
      }
      else if (myCurrentWorkingDirectory != null) {
        // Handle projects with no pubspecs.
        final String projectPath = myCurrentWorkingDirectory.getPath();
        if (filePath.startsWith(projectPath)) {
          result.add(myRemoteProjectRootUri + filePath.substring(projectPath.length()));
        }
      }
    }

    // Bazel / "dart.projects.without.pubspec" case
    if (Registry.is("dart.projects.without.pubspec", false)) {
      if (myBazelWorkspacePath == null || !filePath.startsWith(myBazelWorkspacePath)) {
        final VirtualFile workspaceVFile = DartBazelFileUtil.getBazelWorkspace(file);
        if (workspaceVFile != null) {
          myBazelWorkspacePath = workspaceVFile.getPath();
        }
      }
      if (myBazelWorkspacePath != null) {
        final int libIndex = filePath.lastIndexOf("lib/");
        if (libIndex != -1) {
          result.add(DartUrlResolver.PACKAGE_PREFIX +
                     filePath.substring(myBazelWorkspacePath.length() + 1, libIndex - 1).replace("/", ".") +
                     "/" + filePath.substring(libIndex + "lib/".length()));
        }
      }
    }
    return result;
  }

  public @Nullable XSourcePosition getSourcePosition(@NotNull String isolateId, @NotNull ScriptRef scriptRef, int tokenPos) {
    VirtualFile file = ReadAction.compute(() -> {
      String uri = scriptRef.getUri();

      if (myDASExecutionContextId != null && !isDartPatchUri(uri)) {
        final String path =
          DartAnalysisServerService.getInstance(getSession().getProject()).execution_mapUri(myDASExecutionContextId, null, uri);
        if (path != null) {
          return LocalFileSystem.getInstance().findFileByPath(path);
        }
      }

      final VirtualFile pubspec = myDartUrlResolver.getPubspecYamlFile();
      if (myRemoteProjectRootUri != null && uri.startsWith(myRemoteProjectRootUri) && pubspec != null) {
        final String localRootUri = StringUtil.trimEnd(myDartUrlResolver.getDartUrlForFile(pubspec.getParent()), '/');
        LOG.assertTrue(localRootUri.startsWith(DartUrlResolver.FILE_PREFIX), localRootUri);

        uri = localRootUri + uri.substring(myRemoteProjectRootUri.length());
      }

      if (uri.startsWith(ORG_DARTLANG_APP_PREFIX) && myCurrentWorkingDirectory != null) {
        final String relativeFromCWD = uri.substring(ORG_DARTLANG_APP_PREFIX.length());
        return LocalFileSystem.getInstance().findFileByPath(myCurrentWorkingDirectory.getPath() + relativeFromCWD);
      }

      if (Registry.is("dart.projects.without.pubspec", false) &&
          myBazelWorkspacePath != null &&
          uri.startsWith(DartUrlResolver.PACKAGE_PREFIX)) {
        final int slashIndex = uri.indexOf('/');
        if (slashIndex != -1) {
          final String packageName = uri.substring(DartUrlResolver.PACKAGE_PREFIX.length(), slashIndex).replace('.', '/');
          return LocalFileSystem.getInstance()
            .findFileByPath(myBazelWorkspacePath + "/" + packageName + "/lib" + uri.substring(slashIndex));
        }
      }
      return myDartUrlResolver.findFileByDartUrl(uri);
    });

    if (file == null) {
      file = myScriptIdToContentMap.get(scriptRef.getId());
    }

    TIntObjectHashMap<Pair<Integer, Integer>> tokenPosToLineAndColumn = myScriptIdToLinesAndColumnsMap.get(scriptRef.getId());

    if (file != null && tokenPosToLineAndColumn != null) {
      final Pair<Integer, Integer> lineAndColumn = tokenPosToLineAndColumn.get(tokenPos);
      if (lineAndColumn == null) return XDebuggerUtil.getInstance().createPositionByOffset(file, 0);
      return XDebuggerUtil.getInstance().createPosition(file, lineAndColumn.first, lineAndColumn.second);
    }

    final Script script = myVmServiceWrapper.getScriptSync(isolateId, scriptRef.getId());
    if (script == null) return null;

    String scriptSource = script.getSource();
    if (scriptSource == null) return null;

    if (file == null) {
      file = new LightVirtualFile(PathUtil.getFileName(script.getUri()), DartFileType.INSTANCE, scriptSource);
      ((LightVirtualFile)file).setWritable(false);
      myScriptIdToContentMap.put(scriptRef.getId(), (LightVirtualFile)file);
    }

    if (tokenPosToLineAndColumn == null) {
      List<List<Integer>> table = script.getTokenPosTable();
      if (table != null) {
        tokenPosToLineAndColumn = createTokenPosToLineAndColumnMap(table);
        myScriptIdToLinesAndColumnsMap.put(scriptRef.getId(), tokenPosToLineAndColumn);
      }
    }

    final Pair<Integer, Integer> lineAndColumn = tokenPosToLineAndColumn != null ? tokenPosToLineAndColumn.get(tokenPos) : null;
    if (lineAndColumn == null) return XDebuggerUtil.getInstance().createPositionByOffset(file, 0);

    return XDebuggerUtil.getInstance().createPosition(file, lineAndColumn.first, lineAndColumn.second);
  }

  private static boolean isDartPatchUri(@NotNull String uri) {
    // dart:_builtin or dart:core-patch/core_patch.dart
    return uri.startsWith("dart:_") || uri.startsWith("dart:") && uri.contains("-patch/");
  }

  private static @NotNull TIntObjectHashMap<Pair<Integer, Integer>> createTokenPosToLineAndColumnMap(@NotNull List<List<Integer>> tokenPosTable) {
    // Each subarray consists of a line number followed by (tokenPos, columnNumber) pairs
    // see https://github.com/dart-lang/vm_service_drivers/blob/master/dart/tool/service.md#script
    final TIntObjectHashMap<Pair<Integer, Integer>> result = new TIntObjectHashMap<>();

    for (List<Integer> lineAndPairs : tokenPosTable) {
      final Iterator<Integer> iterator = lineAndPairs.iterator();
      int line = Math.max(0, iterator.next() - 1);
      while (iterator.hasNext()) {
        final int tokenPos = iterator.next();
        final int column = Math.max(0, iterator.next() - 1);
        result.put(tokenPos, Pair.create(line, column));
      }
    }

    return result;
  }

  public @Nullable String getCurrentIsolateId() {
    if (myLatestCurrentIsolateId != null) {
      return myLatestCurrentIsolateId;
    }
    return getIsolateInfos().isEmpty() ? null : getIsolateInfos().iterator().next().getIsolateId();
  }

  @Override
  public @Nullable XDebuggerEvaluator getEvaluator() {
    XStackFrame frame = getSession().getCurrentStackFrame();
    if (frame != null) {
      return frame.getEvaluator();
    }
    return new DartVmServiceEvaluator(this);
  }

  private static @NotNull String threeSlashize(@NotNull String uri) {
    if (!uri.startsWith("file:")) return uri;
    if (uri.startsWith("file:///")) return uri;
    if (uri.startsWith("file://")) return "file:///" + uri.substring("file://".length());
    if (uri.startsWith("file:/")) return "file:///" + uri.substring("file:/".length());
    if (uri.startsWith("file:")) return "file:///" + uri.substring("file:".length());
    return uri;
  }
}
