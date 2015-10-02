package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import com.jetbrains.lang.dart.ide.runner.server.OpenDartObservatoryUrlAction;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.dartlang.vm.service.VmService;
import org.dartlang.vm.service.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class DartVmServiceDebugProcess extends XDebugProcess {
  public static final Logger LOG = Logger.getInstance(DartVmServiceDebugProcess.class.getName());

  @Nullable private final ExecutionResult myExecutionResult;
  @NotNull private final DartUrlResolver myDartUrlResolver;
  private final int myObservatoryPort;

  private VmServiceWrapper myVmServiceWrapper;

  private boolean myVmConnected = false;

  public DartVmServiceDebugProcess(@NotNull final XDebugSession session,
                                   @Nullable final String debuggingHost,
                                   final int observatoryPort,
                                   @Nullable final ExecutionResult executionResult,
                                   @NotNull final DartUrlResolver dartUrlResolver) {
    super(session);
    myExecutionResult = executionResult;
    myDartUrlResolver = dartUrlResolver;
    myObservatoryPort = observatoryPort;
    setLogger();
  }

  private void setLogger() {
    Logging.setLogger(new org.dartlang.vm.service.logging.Logger() {
      @Override
      public void logError(final String message) {
        getSession().getConsoleView().print("Error: " + message + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        LOG.warn(message);
      }

      @Override
      public void logError(final String message, final Throwable exception) {
        getSession().getConsoleView().print("Error: " + message + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        LOG.warn(message, exception);
      }

      @Override
      public void logInformation(final String message) {
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
      myVmServiceWrapper = new VmServiceWrapper(vmService);
      scheduleDebugStartup();
    }
    catch (IOException e) {
      final String message = "Failed to connect to the VM observatory service: " + e.getMessage() + "\n";
      getSession().getConsoleView().print(message, ConsoleViewContentType.ERROR_OUTPUT);
      getSession().stop();
    }
  }

  private void scheduleDebugStartup() {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          myVmServiceWrapper.streamListenSync(VmService.DEBUG_STREAM_ID);
          myVmServiceWrapper.streamListenSync(VmService.ISOLATE_STREAM_ID);
          myVmServiceWrapper.resumeAllIsolatesSync();

          myVmConnected = true;
          getSession().rebuildViews();
        }
        catch (VmServiceException e) {
          final String message = "Failed to start debugging session.\n" + e.getMessage() + "\n";
          getSession().getConsoleView().print(message, ConsoleViewContentType.ERROR_OUTPUT);
          getSession().stop();
        }
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
    return XBreakpointHandler.EMPTY_ARRAY;
  }

  @Override
  public void startStepOver() {
  }

  @Override
  public void startStepInto() {
  }

  @Override
  public void startStepOut() {
  }

  @Override
  public void stop() {
    if (myVmServiceWrapper != null) {
      Disposer.dispose(myVmServiceWrapper);
    }
  }

  @Override
  public void resume() {
  }

  @Override
  public void startPausing() {
  }

  @Override
  public void runToPosition(@NotNull XSourcePosition position) {
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
      topToolbar.addAction(new OpenDartObservatoryUrlAction(myObservatoryPort, new Computable<Boolean>() {
        @Override
        public Boolean compute() {
          return myVmConnected && !getSession().isStopped();
        }
      }));
    }
  }
}
