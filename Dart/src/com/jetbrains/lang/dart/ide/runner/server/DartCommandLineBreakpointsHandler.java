package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ThrowableRunnable;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import com.jetbrains.lang.dart.ide.runner.server.google.*;
import gnu.trove.THashMap;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.LOG;
import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.threeSlashizeFileUrl;

public class DartCommandLineBreakpointsHandler {
  private final DartCommandLineDebugProcess myDebugProcess;
  private final XBreakpointHandler<?>[] myBreakpointHandlers;
  private final Collection<XLineBreakpoint<?>> myInitialBreakpoints = new ArrayList<XLineBreakpoint<?>>();
  private final Map<XLineBreakpoint<?>, List<VmBreakpoint>> myCreatedBreakpoints = new THashMap<XLineBreakpoint<?>, List<VmBreakpoint>>();
  private final TIntObjectHashMap<XLineBreakpoint<?>> myIndexToBreakpointMap = new TIntObjectHashMap<XLineBreakpoint<?>>();

  public DartCommandLineBreakpointsHandler(final @NotNull DartCommandLineDebugProcess debugProcess) {
    myDebugProcess = debugProcess;

    myBreakpointHandlers = new XBreakpointHandler[]{
      new XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>(DartLineBreakpointType.class) {
        public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
          if (myDebugProcess.isVmConnected()) {
            doRegisterBreakpoint(breakpoint);
          }
          else {
            myInitialBreakpoints.add(breakpoint);
          }
        }

        public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint, final boolean temporary) {
          doUnregisterBreakpoint(breakpoint);
        }
      }
    };
  }

  XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointHandlers;
  }

  void registerInitialBreakpoints() {
    for (XLineBreakpoint<?> breakpoint : myInitialBreakpoints) {
      doRegisterBreakpoint(breakpoint);
    }
    //myInitialBreakpoints.clear(); do not clear - it is used later in hasInitialBreakpointHere()
  }

  boolean hasInitialBreakpointHere(final @Nullable VmLocation vmLocation) {
    if (vmLocation == null) return false;

    for (XLineBreakpoint<?> breakpoint : myInitialBreakpoints) {
      final XSourcePosition sourcePosition = breakpoint.getSourcePosition();
      if (sourcePosition != null &&
          threeSlashizeFileUrl(sourcePosition.getFile().getUrl()).equals(threeSlashizeFileUrl(vmLocation.getUnescapedUrl())) &&
          sourcePosition.getLine() == vmLocation.getLineNumber(myDebugProcess.getVmConnection()) - 1) {
        return true;
      }
    }

    return false;
  }

  private void doUnregisterBreakpoint(final XLineBreakpoint<XBreakpointProperties> breakpoint) {
    final XSourcePosition position = breakpoint.getSourcePosition();
    if (position == null) return;
    if (position.getFile().getFileType() != DartFileType.INSTANCE) return;

    suspendPerformActionAndResume(new ThrowableRunnable<IOException>() {
      public void run() throws IOException {
        // see com.google.dart.tools.debug.core.server.ServerBreakpointManager#breakpointRemoved()
        final List<VmBreakpoint> breakpoints = myCreatedBreakpoints.remove(breakpoint);

        if (breakpoints != null) {
          for (VmBreakpoint vmBreakpoint : breakpoints) {
            myDebugProcess.getVmConnection().removeBreakpoint(vmBreakpoint.getIsolate(), vmBreakpoint);
          }
        }
      }
    });
  }

  private void doRegisterBreakpoint(final XLineBreakpoint<?> breakpoint) {
    final XSourcePosition position = breakpoint.getSourcePosition();
    if (position == null) return;
    if (position.getFile().getFileType() != DartFileType.INSTANCE) return;

    final VmIsolate isolate = myDebugProcess.getMainIsolate();
    if (isolate == null) return;

    suspendPerformActionAndResume(new ThrowableRunnable<IOException>() {
      public void run() throws IOException {
        final String dartUrl = myDebugProcess.getDartUrlResolver().getDartUrlForFile(position.getFile());
        final int line = position.getLine() + 1;
        sendSetBreakpointCommand(isolate, breakpoint, dartUrl, line);
      }
    });
  }

  private void suspendPerformActionAndResume(final ThrowableRunnable<IOException> action) {
    final VmIsolate isolate = myDebugProcess.getMainIsolate();
    if (isolate == null) return;

    final Runnable runnable = new Runnable() {
      public void run() {
        // see com.google.dart.tools.debug.core.server.ServerBreakpointManager#addBreakpoint()
        try {
          final VmInterruptResult interruptResult = myDebugProcess.getVmConnection().interruptConditionally(isolate);
          action.run();
          interruptResult.resume();
        }
        catch (IOException exception) {
          LOG.error(exception);
        }
      }
    };

    if (ApplicationManager.getApplication().isDispatchThread()) {
      ApplicationManager.getApplication().executeOnPooledThread(runnable);
    }
    else {
      runnable.run();
    }
  }

  private void sendSetBreakpointCommand(final VmIsolate isolate,
                                        final XLineBreakpoint<?> breakpoint,
                                        final String url,
                                        final int line) throws IOException {
    myDebugProcess.getVmConnection().setBreakpoint(isolate, url, line, new VmCallback<VmBreakpoint>() {
      @Override
      public void handleResult(VmResult<VmBreakpoint> result) {
        if (result.isError()) {
          myDebugProcess.getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_invalid_breakpoint, result.getError());
        }
        else {
          addCreatedBreakpoint(breakpoint, result.getResult());
        }
      }
    });
  }

  private void addCreatedBreakpoint(final XLineBreakpoint<?> breakpoint, final VmBreakpoint vmBreakpoint) {
    List<VmBreakpoint> vmBreakpoints = myCreatedBreakpoints.get(breakpoint);

    if (vmBreakpoints == null) {
      vmBreakpoints = new ArrayList<VmBreakpoint>();
      myCreatedBreakpoints.put(breakpoint, vmBreakpoints);
    }

    vmBreakpoints.add(vmBreakpoint);
    myIndexToBreakpointMap.put(vmBreakpoint.getBreakpointId(), breakpoint);
  }

  public void breakpointResolved(final VmBreakpoint vmBreakpoint) {
    final XLineBreakpoint<?> breakpoint = myIndexToBreakpointMap.get(vmBreakpoint.getBreakpointId());
    if (breakpoint != null) {
      myDebugProcess.getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_verified_breakpoint, null);
    }
    else {
      LOG.info("Unknown breakpoint id: " + vmBreakpoint.getBreakpointId());
    }
    // breakpoint could be automatically shifted down to another line if there's no code at initial line
    // breakpoint icon on the gutter is shifted in DartEditor (see com.google.dart.tools.debug.core.server.ServerBreakpointManager#handleBreakpointResolved)
    // but we prefer to keep it at its original position
  }
}
