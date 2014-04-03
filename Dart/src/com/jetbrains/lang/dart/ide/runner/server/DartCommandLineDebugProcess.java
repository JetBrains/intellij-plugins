package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DartCommandLineDebugProcess extends XDebugProcess {
  public static final Logger LOG = Logger.getInstance(DartCommandLineDebugProcess.class.getName());

  private final @Nullable ExecutionResult myExecutionResult;
  private final VmConnection myVmConnection;
  private final DartCommandLineBreakpointsHandler myBreakpointsHandler;
  private final @Nullable VirtualFile myPubspecYamlFile;
  private final @NotNull List<VirtualFile> myPackageRoots;
  private boolean myVmConnected;
  private @Nullable VmIsolate myMainIsolate;

  public DartCommandLineDebugProcess(final @NotNull XDebugSession session,
                                     final int debuggingPort,
                                     final @Nullable ExecutionResult executionResult,
                                     final @NotNull String dartScriptPath) {
    super(session);

    final VirtualFile dartFile = LocalFileSystem.getInstance().findFileByPath(dartScriptPath);
    if (dartFile != null) {
      final Pair<VirtualFile, List<VirtualFile>> yamlAndPackageRoots =
        PubspecYamlUtil.getPubspecYamlFileAndDartPackageRoots(session.getProject(), dartFile);
      myPubspecYamlFile = yamlAndPackageRoots.first;
      myPackageRoots = yamlAndPackageRoots.second;
    }
    else {
      myPubspecYamlFile = null;
      myPackageRoots = Collections.emptyList();
    }

    myBreakpointsHandler = new DartCommandLineBreakpointsHandler(this);
    myExecutionResult = executionResult;

    // see com.google.dart.tools.debug.core.server.ServerDebugTarget
    myVmConnection = new VmConnection(null, debuggingPort);
    myVmConnection.addListener(new DartVmListener(this));
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
    return myExecutionResult != null ? myExecutionResult.getProcessHandler() : null;
  }

  @NotNull
  @Override
  public ExecutionConsole createConsole() {
    if (myExecutionResult != null) {
      return myExecutionResult.getExecutionConsole();
    }
    return super.createConsole();
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new DartDebuggerEditorsProvider();
  }

  @NotNull
  public DartCommandLineBreakpointsHandler getDartBreakpointsHandler() {
    return myBreakpointsHandler;
  }

  @Override
  @NotNull
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointsHandler.getBreakpointHandlers();
  }

  @Override
  public void startStepOver() {
    if (myMainIsolate != null) {
      try {
        myVmConnection.stepOver(myMainIsolate);
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void startStepInto() {
    if (myMainIsolate != null) {
      try {
        myVmConnection.stepInto(myMainIsolate);
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void startStepOut() {
    if (myMainIsolate != null) {
      try {
        myVmConnection.stepOut(myMainIsolate);
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
    if (myMainIsolate != null) {
      try {
        myVmConnection.resume(myMainIsolate);
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void startPausing() {
    if (myMainIsolate != null) {
      try {
        myVmConnection.interrupt(myMainIsolate);
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

  @Nullable
  public VirtualFile getPubspecYamlFile() {
    return myPubspecYamlFile;
  }

  @NotNull
  public List<VirtualFile> getPackageRoots() {
    return myPackageRoots;
  }

  public VmConnection getVmConnection() {
    return myVmConnection;
  }

  @Nullable
  public VmIsolate getMainIsolate() {
    return myMainIsolate;
  }

  boolean isVmConnected() {
    return myVmConnected;
  }

  void setVmConnected(final boolean vmConnected) {
    myVmConnected = vmConnected;
    getSession().rebuildViews();
  }

  void isolateCreated(final VmIsolate vmIsolate) {
    if (myMainIsolate == null) {
      myMainIsolate = vmIsolate;
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

  public static String threeSlashizeFileUrl(final String fileUrl) {
    if (!fileUrl.startsWith("file:///")) {
      if (fileUrl.startsWith("file://")) {
        return "file:///" + fileUrl.substring("file://".length());
      }
      if (fileUrl.startsWith("file:/")) {
        return "file:///" + fileUrl.substring("file:/".length());
      }
    }
    return fileUrl;
  }
}
