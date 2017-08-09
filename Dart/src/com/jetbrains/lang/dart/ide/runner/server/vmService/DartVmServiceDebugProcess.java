package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.google.common.base.Charsets;
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
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
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
import com.intellij.util.containers.HashSet;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.actions.DartPopFrameAction;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import com.jetbrains.lang.dart.ide.runner.server.OpenDartObservatoryUrlAction;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceSuspendContext;
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

  @Nullable private final ExecutionResult myExecutionResult;
  @NotNull private final DartUrlResolver myDartUrlResolver;
  @NotNull private final String myDebuggingHost;
  private final int myObservatoryPort;

  private boolean myVmConnected = false;

  @NotNull private final XBreakpointHandler[] myBreakpointHandlers;
  private final IsolatesInfo myIsolatesInfo;
  private VmServiceWrapper myVmServiceWrapper;

  @NotNull private final Set<String> mySuspendedIsolateIds = Collections.synchronizedSet(new THashSet<String>());
  private String myLatestCurrentIsolateId;

  private final Map<String, LightVirtualFile> myScriptIdToContentMap = new THashMap<>();
  private final Map<String, TIntObjectHashMap<Pair<Integer, Integer>>> myScriptIdToLinesAndColumnsMap =
    new THashMap<>();

  @Nullable private final String myDASExecutionContextId;
  private final boolean myRemoteDebug;
  private final int myTimeout;
  @Nullable private final VirtualFile myCurrentWorkingDirectory;
  @Nullable protected String myRemoteProjectRootUri;

  @NotNull private final OpenDartObservatoryUrlAction myOpenObservatoryAction =
    new OpenDartObservatoryUrlAction(null, () -> myVmConnected && !getSession().isStopped());


  public DartVmServiceDebugProcess(@NotNull final XDebugSession session,
                                   @NotNull final String debuggingHost,
                                   final int observatoryPort,
                                   @Nullable final ExecutionResult executionResult,
                                   @NotNull final DartUrlResolver dartUrlResolver,
                                   @Nullable final String dasExecutionContextId,
                                   final boolean remoteDebug,
                                   final int timeout,
                                   @Nullable final VirtualFile currentWorkingDirectory) {
    super(session);
    myDebuggingHost = debuggingHost;
    myObservatoryPort = observatoryPort;
    myExecutionResult = executionResult;
    myDartUrlResolver = dartUrlResolver;
    myRemoteDebug = remoteDebug;
    myTimeout = timeout;
    myCurrentWorkingDirectory = currentWorkingDirectory;

    myIsolatesInfo = new IsolatesInfo();

    myBreakpointHandlers = new XBreakpointHandler[]{
      new DartVmServiceBreakpointHandler(this),
      new DartExceptionBreakpointHandler(this)
    };

    setLogger();

    session.addSessionListener(new XDebugSessionListener() {
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

    myDASExecutionContextId = dasExecutionContextId;

    if (remoteDebug) {
      // TODO won't work since Dart SDK 1.22 because auth token in URL is required
      scheduleConnect(getObservatoryUrl("ws", "/ws"));
    }
    else {
      getProcessHandler().addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(@NotNull final ProcessEvent event, @NotNull final Key outputType) {
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

    if (remoteDebug) {
      LOG.assertTrue(myExecutionResult == null && myDASExecutionContextId == null, myDASExecutionContextId + myExecutionResult);
    }
    else {
      LOG.assertTrue(myExecutionResult != null && myDASExecutionContextId != null, myDASExecutionContextId + myExecutionResult);
    }
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

  protected void scheduleConnect(@NotNull final String url) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      long timeout = (long)myTimeout;
      long startTime = System.currentTimeMillis();

      try {
        while (true) {
          try {
            connect(url);
            break;
          }
          catch (IOException e) {
            if (System.currentTimeMillis() > startTime + timeout) {
              throw e;
            }
            else {
              TimeoutUtil.sleep(50);
            }
          }
        }
      }
      catch (IOException e) {
        String message = "Failed to connect to the VM observatory service: " + e.toString() + "\n";
        Throwable cause = e.getCause();
        while (cause != null) {
          message += "Caused by: " + cause.toString() + "\n";
          final Throwable cause1 = cause.getCause();
          if (cause1 != cause) {
            cause = cause1;
          }
        }

        getSession().getConsoleView().print(message, ConsoleViewContentType.ERROR_OUTPUT);
        getSession().stop();
      }
    });
  }

  private void connect(@NotNull final String url) throws IOException {
    final VmService vmService = VmService.connect(url);
    final DartVmServiceListener vmServiceListener =
      new DartVmServiceListener(this, (DartVmServiceBreakpointHandler)myBreakpointHandlers[0]);

    vmService.addVmServiceListener(vmServiceListener);

    myVmServiceWrapper =
      new VmServiceWrapper(this, vmService, vmServiceListener, myIsolatesInfo, (DartVmServiceBreakpointHandler)myBreakpointHandlers[0]);
    myVmServiceWrapper.handleDebuggerConnected();

    myVmConnected = true;
  }

  @NotNull
  @Deprecated // returns incorrect URL for Dart SDK 1.22+ because returned URL doesn't contain auth token
  private String getObservatoryUrl(@NotNull final String scheme, @Nullable final String path) {
    return scheme + "://" + myDebuggingHost + ":" + myObservatoryPort + StringUtil.notNullize(path);
  }

  @Override
  protected ProcessHandler doGetProcessHandler() {
    return myExecutionResult == null ? super.doGetProcessHandler() : myExecutionResult.getProcessHandler();
  }

  @NotNull
  @Override
  public ExecutionConsole createConsole() {
    return myExecutionResult == null ? super.createConsole() : myExecutionResult.getExecutionConsole();
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new DartDebuggerEditorsProvider();
  }

  @Override
  @NotNull
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointHandlers;
  }

  public boolean isRemoteDebug() {
    return myRemoteDebug;
  }

  public void guessRemoteProjectRoot(@NotNull final ElementList<LibraryRef> libraries) {
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

  public void isolateSuspended(@NotNull final IsolateRef isolateRef) {
    mySuspendedIsolateIds.add(isolateRef.getId());
  }

  public boolean isIsolateSuspended(@NotNull final String isolateId) {
    return mySuspendedIsolateIds.contains(isolateId);
  }

  public boolean isIsolateAlive(@NotNull final String isolateId) {
    for (IsolatesInfo.IsolateInfo isolateInfo : myIsolatesInfo.getIsolateInfos()) {
      if (isolateId.equals(isolateInfo.getIsolateId())) {
        return true;
      }
    }
    return false;
  }

  public void isolateResumed(@NotNull final IsolateRef isolateRef) {
    mySuspendedIsolateIds.remove(isolateRef.getId());
  }

  public void isolateExit(@NotNull final IsolateRef isolateRef) {
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
             : DartBundle.message("debugger.trying.to.connect.vm.at.0", getObservatoryUrl("ws", "/ws"));
  }

  @Override
  public void registerAdditionalActions(@NotNull final DefaultActionGroup leftToolbar,
                                        @NotNull final DefaultActionGroup topToolbar,
                                        @NotNull final DefaultActionGroup settings) {
    // For Run tool window this action is added in DartCommandLineRunningState.createActions()
    topToolbar.addSeparator();
    topToolbar.addAction(myOpenObservatoryAction);
    topToolbar.addAction(new DartPopFrameAction());
  }

  @NotNull
  public Collection<String> getUrisForFile(@NotNull final VirtualFile file) {
    final Set<String> result = new HashSet<>();
    String uriByIde = myDartUrlResolver.getDartUrlForFile(file);

    // If dart:, short circuit the results.
    if (uriByIde.startsWith(DartUrlResolver.DART_PREFIX)) {
      result.add(uriByIde);
      return result;
    }

    // file:
    if (uriByIde.startsWith(DartUrlResolver.FILE_PREFIX)) {
      result.add(threeSlashize(uriByIde));
    }
    else {
      result.add(uriByIde);
      result.add(threeSlashize(new File(file.getPath()).toURI().toString()));
    }

    // straight path - used by some VM embedders
    result.add(file.getPath());

    // package: (if applicable)
    if (myDASExecutionContextId != null) {
      final String uriByServer =
        DartAnalysisServerService.getInstance(getSession().getProject()).execution_mapUri(myDASExecutionContextId, file.getPath(), null);
      if (uriByServer != null) {
        result.add(uriByServer);
      }
    }

    // remote prefix (if applicable)
    if (myRemoteProjectRootUri != null) {
      final VirtualFile pubspec = myDartUrlResolver.getPubspecYamlFile();
      if (pubspec != null) {
        final String projectPath = pubspec.getParent().getPath();
        final String filePath = file.getPath();
        if (filePath.startsWith(projectPath)) {
          result.add(myRemoteProjectRootUri + filePath.substring(projectPath.length()));
        }
      }
      else if (myCurrentWorkingDirectory != null) {
        // Handle projects with no pubspecs.
        final String projectPath = myCurrentWorkingDirectory.getPath();
        final String filePath = file.getPath();
        if (filePath.startsWith(projectPath)) {
          result.add(myRemoteProjectRootUri + filePath.substring(projectPath.length()));
        }
      }
    }

    return result;
  }

  @Nullable
  public XSourcePosition getSourcePosition(@NotNull final String isolateId, @NotNull final ScriptRef scriptRef, int tokenPos) {
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

    if (file == null) {
      file = new LightVirtualFile(PathUtil.getFileName(script.getUri()), DartFileType.INSTANCE, script.getSource());
      ((LightVirtualFile)file).setWritable(false);
      myScriptIdToContentMap.put(scriptRef.getId(), (LightVirtualFile)file);
    }

    if (tokenPosToLineAndColumn == null) {
      tokenPosToLineAndColumn = createTokenPosToLineAndColumnMap(script.getTokenPosTable());
      myScriptIdToLinesAndColumnsMap.put(scriptRef.getId(), tokenPosToLineAndColumn);
    }

    final Pair<Integer, Integer> lineAndColumn = tokenPosToLineAndColumn.get(tokenPos);
    if (lineAndColumn == null) return XDebuggerUtil.getInstance().createPositionByOffset(file, 0);
    return XDebuggerUtil.getInstance().createPosition(file, lineAndColumn.first, lineAndColumn.second);
  }

  private static boolean isDartPatchUri(@NotNull final String uri) {
    // dart:_builtin or dart:core-patch/core_patch.dart
    return uri.startsWith("dart:_") || uri.startsWith("dart:") && uri.contains("-patch/");
  }

  @NotNull
  private static TIntObjectHashMap<Pair<Integer, Integer>> createTokenPosToLineAndColumnMap(@NotNull final List<List<Integer>> tokenPosTable) {
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

  @NotNull
  private static String threeSlashize(@NotNull final String uri) {
    if (!uri.startsWith("file:")) return uri;
    if (uri.startsWith("file:///")) return uri;
    if (uri.startsWith("file://")) return "file:///" + uri.substring("file://".length());
    if (uri.startsWith("file:/")) return "file:///" + uri.substring("file:/".length());
    if (uri.startsWith("file:")) return "file:///" + uri.substring("file:".length());
    return uri;
  }
}
