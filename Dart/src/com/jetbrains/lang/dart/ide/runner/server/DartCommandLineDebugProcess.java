package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import com.jetbrains.lang.dart.ide.runner.server.frame.DartStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.google.VmConnection;
import com.jetbrains.lang.dart.ide.runner.server.google.VmIsolate;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class DartCommandLineDebugProcess extends XDebugProcess {
  public static final Logger LOG = Logger.getInstance(DartCommandLineDebugProcess.class.getName());

  // just a few files, several kBytes each, won't cause OOM
  private static final Map<String, LightVirtualFile> DART_SDK_PATCH_FILES = new THashMap<>();

  private final @Nullable ExecutionResult myExecutionResult;
  private final @NotNull DartUrlResolver myDartUrlResolver;
  private final @NotNull XBreakpointHandler[] myBreakpointHandlers;
  private final @NotNull VmConnection myVmConnection;
  private final int myObservatoryPort;

  private final @NotNull LinkedList<VmIsolate> myAllIsolates = new LinkedList<>();
  private final @NotNull Set<VmIsolate> mySuspendedIsolates = new THashSet<>();
  private VmIsolate myLatestCurrentIsolate;

  private boolean myVmConnected = false;

  public DartCommandLineDebugProcess(@NotNull final XDebugSession session,
                                     @Nullable final String debuggingHost,
                                     final int debuggingPort,
                                     final int observatoryPort,
                                     @Nullable final ExecutionResult executionResult,
                                     @NotNull final DartUrlResolver dartUrlResolver) {
    super(session);
    myExecutionResult = executionResult;
    myDartUrlResolver = dartUrlResolver;
    myObservatoryPort = observatoryPort;

    final DartCommandLineBreakpointHandler dartBreakpointHandler = new DartCommandLineBreakpointHandler(this);
    myBreakpointHandlers = new XBreakpointHandler[]{dartBreakpointHandler};

    // see com.google.dart.tools.debug.core.server.ServerDebugTarget
    myVmConnection = new VmConnection(debuggingHost, debuggingPort);
    myVmConnection.addListener(new DartVmListener(this, dartBreakpointHandler));

    session.addSessionListener(new XDebugSessionAdapter() {
      @Override
      public void sessionPaused() {
        stackFrameChanged();
      }

      @Override
      public void stackFrameChanged() {
        final XStackFrame stackFrame = getSession().getCurrentStackFrame();
        myLatestCurrentIsolate = stackFrame instanceof DartStackFrame ? ((DartStackFrame)stackFrame).getIsolate() : null;
      }
    });

    connect();
  }

  private void connect() {
    // see com.google.dart.tools.debug.core.server.ServerDebugTarget.connect()

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      long timeout = 10000;
      long startTime = System.currentTimeMillis();

      try {
        TimeoutUtil.sleep(50);

        while (true) {
          try {
            myVmConnection.connect();
            break;
          }
          catch (IOException ioe) {
            if (!myVmConnection.isConnected()) {
              throw ioe;
            }

            if (System.currentTimeMillis() > startTime + timeout) {
              throw ioe;
            }
            else {
              TimeoutUtil.sleep(20);
            }
          }
        }
      }
      catch (IOException ioe) {
        getSession().getConsoleView()
          .print("Unable to connect debugger to the Dart VM: " + ioe.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
        getSession().stop();
      }
    });
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

  @Override
  public void startStepOver(@Nullable XSuspendContext context) {
    if (myLatestCurrentIsolate != null && mySuspendedIsolates.contains(myLatestCurrentIsolate)) {
      try {
        myVmConnection.stepOver(myLatestCurrentIsolate);
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void startStepInto(@Nullable XSuspendContext context) {
    if (myLatestCurrentIsolate != null && mySuspendedIsolates.contains(myLatestCurrentIsolate)) {
      try {
        myVmConnection.stepInto(myLatestCurrentIsolate);
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void startStepOut(@Nullable XSuspendContext context) {
    if (myLatestCurrentIsolate != null && mySuspendedIsolates.contains(myLatestCurrentIsolate)) {
      try {
        myVmConnection.stepOut(myLatestCurrentIsolate);
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void stop() {
    try {
      LOG.debug("closing connection");
      myVmConnection.close();
    }
    catch (IOException e) {
      LOG.warn(e);
    }
  }

  @Override
  public void resume(@Nullable XSuspendContext context) {
    try {
      for (VmIsolate isolate : new THashSet<>(mySuspendedIsolates)) {
        myVmConnection.resume(isolate);
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

  @Override
  public void startPausing() {
    if (!myAllIsolates.isEmpty() && mySuspendedIsolates.isEmpty()) {
      try {
        for (VmIsolate isolate : myAllIsolates) {
          myVmConnection.interrupt(isolate);
        }
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
    // todo implement
    resume(context);
  }

  @NotNull
  public DartUrlResolver getDartUrlResolver() {
    return myDartUrlResolver;
  }

  @NotNull
  public VmConnection getVmConnection() {
    return myVmConnection;
  }

  public void setVmConnected(final boolean vmConnected) {
    myVmConnected = vmConnected;
    getSession().rebuildViews();
  }

  public void isolateCreated(final VmIsolate isolate) {
    myAllIsolates.add(isolate);
  }

  public void isolateShutdown(final VmIsolate isolate) {
    myAllIsolates.remove(isolate);
  }

  public void isolateSuspended(@NotNull final VmIsolate isolate) {
    mySuspendedIsolates.add(isolate);
  }

  public void isolateResumed(@NotNull final VmIsolate isolate) {
    mySuspendedIsolates.remove(isolate);
  }

  public void processAllIsolates(@NotNull final Consumer<VmIsolate> isolateConsumer) {
    if (!myVmConnected) return;

    for (VmIsolate isolate : myAllIsolates) {
      isolateConsumer.consume(isolate);
    }
  }

  public boolean isIsolateSuspended(@NotNull final VmIsolate isolate) {
    return mySuspendedIsolates.contains(isolate);
  }

  @Override
  public String getCurrentStateMessage() {
    return getSession().isStopped()
           ? XDebuggerBundle.message("debugger.state.message.disconnected")
           : myVmConnected
             ? XDebuggerBundle.message("debugger.state.message.connected")
             : DartBundle.message("debugger.waiting.vm.to.connect");
  }

  @Override
  public void registerAdditionalActions(@NotNull final DefaultActionGroup leftToolbar,
                                        @NotNull final DefaultActionGroup topToolbar,
                                        @NotNull final DefaultActionGroup settings) {
    // For Run tool window this action is added in DartCommandLineRunningState.createActions()
    topToolbar.addSeparator();

    if (myObservatoryPort > 0) {
      topToolbar.addAction(
        new OpenDartObservatoryUrlAction("http://" + NetUtils.getLocalHostString() + ":" + myObservatoryPort,
                                         () -> myVmConnected && !getSession().isStopped()));
    }
  }

  @Nullable
  public VirtualFile findFile(@NotNull final VmIsolate isolate, final int libraryId, @NotNull final String url) {
    final String cacheKey = libraryId + ":" + url;
    final LightVirtualFile cachedFile = DART_SDK_PATCH_FILES.get(cacheKey);
    if (cachedFile != null) {
      return cachedFile;
    }

    VirtualFile file = myDartUrlResolver.findFileByDartUrl(url);

    if (file == null && libraryId != -1 && url.startsWith(DartUrlResolver.DART_PREFIX)) {
      final String content = myVmConnection.getScriptSource(isolate, libraryId, url);
      if (content != null) {
        file = new LightVirtualFile(PathUtil.getFileName(url), content);
        ((LightVirtualFile)file).setWritable(false);
        DART_SDK_PATCH_FILES.put(cacheKey, (LightVirtualFile)file);
      }
    }

    return file;
  }
}
