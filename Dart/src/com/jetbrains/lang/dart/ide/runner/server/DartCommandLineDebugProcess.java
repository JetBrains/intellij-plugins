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
import com.intellij.util.Consumer;
import com.intellij.util.TimeoutUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import com.jetbrains.lang.dart.ide.runner.server.google.VmConnection;
import com.jetbrains.lang.dart.ide.runner.server.google.VmIsolate;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;

public class DartCommandLineDebugProcess extends XDebugProcess {
  public static final Logger LOG = Logger.getInstance(DartCommandLineDebugProcess.class.getName());

  private final @NotNull ExecutionResult myExecutionResult;
  private final @NotNull DartUrlResolver myDartUrlResolver;
  private final @NotNull XBreakpointHandler[] myBreakpointHandlers;
  private final @NotNull VmConnection myVmConnection;
  private final int myObservatoryPort;

  private final @NotNull LinkedList<VmIsolate> myAliveIsolates = new LinkedList<VmIsolate>();
  private boolean myVmConnected = false;
  private VmIsolate mySuspendedIsolate;

  public DartCommandLineDebugProcess(@NotNull final XDebugSession session,
                                     @NotNull final DartCommandLineRunningState commandLineState,
                                     @NotNull final ExecutionResult executionResult,
                                     @NotNull final VirtualFile dartFile) {
    super(session);
    myExecutionResult = executionResult;
    myDartUrlResolver = DartUrlResolver.getInstance(session.getProject(), dartFile);
    myObservatoryPort = commandLineState.getObservatoryPort();

    final DartCommandLineBreakpointHandler dartBreakpointHandler = new DartCommandLineBreakpointHandler(this);
    myBreakpointHandlers = new XBreakpointHandler[]{dartBreakpointHandler};

    // see com.google.dart.tools.debug.core.server.ServerDebugTarget
    myVmConnection = new VmConnection(null, commandLineState.getDebuggingPort());
    myVmConnection.addListener(new DartVmListener(this, dartBreakpointHandler));

    connect();
  }

  private void connect() {
    // see com.google.dart.tools.debug.core.server.ServerDebugTarget.connect()

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
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
      }
    });
  }

  @Override
  protected ProcessHandler doGetProcessHandler() {
    return myExecutionResult.getProcessHandler();
  }

  @NotNull
  @Override
  public ExecutionConsole createConsole() {
    return myExecutionResult.getExecutionConsole();
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
    if (mySuspendedIsolate != null) {
      try {
        myVmConnection.stepOver(mySuspendedIsolate);
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void startStepInto() {
    if (mySuspendedIsolate != null) {
      try {
        myVmConnection.stepInto(mySuspendedIsolate);
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void startStepOut() {
    if (mySuspendedIsolate != null) {
      try {
        myVmConnection.stepOut(mySuspendedIsolate);
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
  public void resume() {
    if (mySuspendedIsolate != null) {
      try {
        myVmConnection.resume(mySuspendedIsolate);
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void startPausing() {
    if (!myAliveIsolates.isEmpty()) {
      try {
        myVmConnection.interrupt(myAliveIsolates.getLast());
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void runToPosition(@NotNull XSourcePosition position) {
    // todo implement
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
    myAliveIsolates.add(isolate);
  }

  public void isolateShutdown(final VmIsolate isolate) {
    myAliveIsolates.remove(isolate);
  }

  public void setSuspendedIsolate(final VmIsolate suspendedIsolate) {
    mySuspendedIsolate = suspendedIsolate; // todo can several isolates be suspended?
  }

  public void processAliveIsolates(@NotNull final Consumer<VmIsolate> consumer) {
    if (!myVmConnected) return;

    for (VmIsolate isolate : myAliveIsolates) {
      consumer.consume(isolate);
    }
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

    topToolbar.addAction(new OpenDartObservatoryUrlAction(myObservatoryPort, new Computable<Boolean>() {
      @Override
      public Boolean compute() {
        return myVmConnected && !getSession().isStopped();
      }
    }));
  }
}
