package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionAdapter;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import com.jetbrains.lang.dart.ide.runner.server.OpenDartObservatoryUrlAction;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceStackFrame;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import gnu.trove.THashSet;
import org.dartlang.vm.service.VmService;
import org.dartlang.vm.service.element.IsolateRef;
import org.dartlang.vm.service.element.StepOption;
import org.dartlang.vm.service.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class DartVmServiceDebugProcess extends XDebugProcess {
  private static final Logger LOG = Logger.getInstance(DartVmServiceDebugProcess.class.getName());

  @Nullable private final ExecutionResult myExecutionResult;
  @NotNull private final DartUrlResolver myDartUrlResolver;
  private final int myObservatoryPort;

  @NotNull private final XBreakpointHandler[] myBreakpointHandlers;
  private final IsolatesInfo myIsolatesInfo;
  private VmServiceWrapper myVmServiceWrapper;

  @NotNull private final Set<String> mySuspendedIsolateIds = new THashSet<String>();
  private String myLatestCurrentIsolateId;

  public DartVmServiceDebugProcess(@NotNull final XDebugSession session,
                                   @Nullable final String debuggingHost,
                                   final int observatoryPort,
                                   @Nullable final ExecutionResult executionResult,
                                   @NotNull final DartUrlResolver dartUrlResolver) {
    super(session);
    myExecutionResult = executionResult;
    myDartUrlResolver = dartUrlResolver;
    myObservatoryPort = observatoryPort;

    myIsolatesInfo = new IsolatesInfo();
    final DartVmServiceBreakpointHandler breakpointHandler = new DartVmServiceBreakpointHandler(this, myIsolatesInfo);
    myBreakpointHandlers = new XBreakpointHandler[]{breakpointHandler};

    setLogger();

    session.addSessionListener(new XDebugSessionAdapter() {
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

        getSession().getConsoleView().print("Error: " + message + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        LOG.warn(message);
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

  @Override
  public void sessionInitialized() {
    try {
      final VmService vmService = VmService.localConnect(myObservatoryPort);
      vmService.addVmServiceListener(new DartVmServiceListener(this, (DartVmServiceBreakpointHandler)myBreakpointHandlers[0]));

      myVmServiceWrapper = new VmServiceWrapper(this, vmService, myIsolatesInfo, (DartVmServiceBreakpointHandler)myBreakpointHandlers[0]);

      myVmServiceWrapper.streamListen(VmService.DEBUG_STREAM_ID);
      myVmServiceWrapper.streamListen(VmService.ISOLATE_STREAM_ID);
      myVmServiceWrapper.handleDebuggerConnected();
    }
    catch (IOException e) {
      final String message = "Failed to connect to the VM observatory service: " + e.getMessage() + "\n";
      getSession().getConsoleView().print(message, ConsoleViewContentType.ERROR_OUTPUT);
      getSession().stop();
    }
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
  public void startStepOver() {
    if (myLatestCurrentIsolateId != null && mySuspendedIsolateIds.contains(myLatestCurrentIsolateId)) {
      myVmServiceWrapper.resumeIsolate(myLatestCurrentIsolateId, StepOption.Over);
    }
  }

  @Override
  public void startStepInto() {
    if (myLatestCurrentIsolateId != null && mySuspendedIsolateIds.contains(myLatestCurrentIsolateId)) {
      myVmServiceWrapper.resumeIsolate(myLatestCurrentIsolateId, StepOption.Into);
    }
  }

  @Override
  public void startStepOut() {
    if (myLatestCurrentIsolateId != null && mySuspendedIsolateIds.contains(myLatestCurrentIsolateId)) {
      myVmServiceWrapper.resumeIsolate(myLatestCurrentIsolateId, StepOption.Out);
    }
  }

  @Override
  public void stop() {
    if (myVmServiceWrapper != null) {
      Disposer.dispose(myVmServiceWrapper);
    }
  }

  @Override
  public void resume() {
    for (String isolateId : mySuspendedIsolateIds) {
      myVmServiceWrapper.resumeIsolate(isolateId);
    }
  }

  @Override
  public void startPausing() {
  }

  @Override
  public void runToPosition(@NotNull XSourcePosition position) {
  }

  public void isolateSuspended(@NotNull final IsolateRef isolateRef) {
    mySuspendedIsolateIds.add(isolateRef.getId());
  }

  public boolean isIsolateSuspended(@NotNull final String isolateId) {
    return mySuspendedIsolateIds.contains(isolateId);
  }

  public void isolateResumed(@NotNull final IsolateRef isolateRef) {
    mySuspendedIsolateIds.remove(isolateRef.getId());
  }

  public void isolateExit(@NotNull final IsolateRef isolateRef) {
    myIsolatesInfo.deleteIsolate(isolateRef);
    mySuspendedIsolateIds.remove(isolateRef.getId());

    if (isolateRef.getId().equals(myLatestCurrentIsolateId)) {
      resume(); // otherwise no way no resume them from UI
    }
  }

  @Override
  public void registerAdditionalActions(@NotNull final DefaultActionGroup leftToolbar,
                                        @NotNull final DefaultActionGroup topToolbar,
                                        @NotNull final DefaultActionGroup settings) {
    // For Run tool window this action is added in DartCommandLineRunningState.createActions()
    topToolbar.addSeparator();

    if (myObservatoryPort > 0) {
      topToolbar.addAction(new OpenDartObservatoryUrlAction(myObservatoryPort, new Computable<Boolean>() {
        @Override
        public Boolean compute() {
          return !getSession().isStopped();
        }
      }));
    }
  }

  @NotNull
  public String getUriForFile(@NotNull final VirtualFile file) {
    // todo check sdk libs, sdk parts, package libs, package parts, libs, parts, slashes
    return threeslashize(myDartUrlResolver.getDartUrlForFile(file));
  }

  @NotNull
  private static String threeslashize(@NotNull final String uri) {
    if (!uri.startsWith("file:")) return uri;
    if (uri.startsWith("file:///")) return uri;
    if (uri.startsWith("file://")) return "file:///" + uri.substring("file://".length());
    if (uri.startsWith("file:/")) return "file:///" + uri.substring("file:/".length());
    if (uri.startsWith("file:")) return "file:///" + uri.substring("file:".length());
    return uri;
  }
}
