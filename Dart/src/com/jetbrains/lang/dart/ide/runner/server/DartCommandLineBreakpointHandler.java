package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Consumer;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.MultiMap;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import com.jetbrains.lang.dart.ide.runner.server.google.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.intellij.icons.AllIcons.Debugger.Db_invalid_breakpoint;
import static com.intellij.icons.AllIcons.Debugger.Db_verified_breakpoint;
import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.LOG;

public class DartCommandLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {
  private final DartCommandLineDebugProcess myDebugProcess;
  private final MultiMap<XLineBreakpoint<?>, VmBreakpoint> myXBreakpointToVmBreakpoints = MultiMap.createSet();
  private final Set<XLineBreakpoint<?>> myXBreakpoints = new THashSet<>();

  public DartCommandLineBreakpointHandler(@NotNull final DartCommandLineDebugProcess debugProcess) {
    super(DartLineBreakpointType.class);
    myDebugProcess = debugProcess;
  }

  public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    myXBreakpoints.add(xBreakpoint);
    myDebugProcess.processAllIsolates(isolate -> doRegisterBreakpoint(isolate, xBreakpoint));
  }

  public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint, final boolean temporary) {
    myXBreakpoints.remove(xBreakpoint);
    myDebugProcess.processAllIsolates(isolate -> doUnregisterBreakpoint(isolate, xBreakpoint));
  }

  private void doRegisterBreakpoint(@NotNull final VmIsolate isolate, @NotNull final XLineBreakpoint<?> xBreakpoint) {
    final XSourcePosition position = xBreakpoint.getSourcePosition();
    if (position == null || position.getFile().getFileType() != DartFileType.INSTANCE) return;

    final String dartUrl = myDebugProcess.getDartUrlResolver().getDartUrlForFile(position.getFile());
    final int line = position.getLine() + 1;

    suspendVmPerformActionAndResume(isolate, () -> myDebugProcess.getVmConnection().setBreakpoint(isolate, dartUrl, line, new VmCallback<VmBreakpoint>() {
      @Override
      public void handleResult(final VmResult<VmBreakpoint> result) {
        if (result.isError()) {
          myDebugProcess.getSession().updateBreakpointPresentation(xBreakpoint, Db_invalid_breakpoint, result.getError());
        }
        else {
          myXBreakpointToVmBreakpoints.putValue(xBreakpoint, result.getResult());
        }
      }
    }));
  }

  private void doUnregisterBreakpoint(@NotNull final VmIsolate isolate, @NotNull final XLineBreakpoint<?> xBreakpoint) {
    final XSourcePosition position = xBreakpoint.getSourcePosition();
    if (position == null || position.getFile().getFileType() != DartFileType.INSTANCE) return;

    suspendVmPerformActionAndResume(isolate, () -> {
      final Collection<VmBreakpoint> vmBreakpoints = myXBreakpointToVmBreakpoints.remove(xBreakpoint);
      if (vmBreakpoints != null) {
        for (VmBreakpoint vmBreakpoint : vmBreakpoints) {
          myDebugProcess.getVmConnection().removeBreakpoint(vmBreakpoint.getIsolate(), vmBreakpoint);
        }
      }
    });
  }

  private void suspendVmPerformActionAndResume(@NotNull final VmIsolate isolate, @NotNull final ThrowableRunnable<IOException> action) {
    final Runnable runnable = () -> {
      try {
        final VmInterruptResult interruptResult = myDebugProcess.getVmConnection().interruptConditionally(isolate);
        action.run();
        interruptResult.resume();
      }
      catch (IOException exception) {
        LOG.error(exception);
      }
    };

    if (ApplicationManager.getApplication().isDispatchThread()) {
      ApplicationManager.getApplication().executeOnPooledThread(runnable);
    }
    else {
      runnable.run();
    }
  }

  public void breakpointResolved(@NotNull final VmBreakpoint vmBreakpoint) {
    for (Map.Entry<XLineBreakpoint<?>, Collection<VmBreakpoint>> entry : myXBreakpointToVmBreakpoints.entrySet()) {
      if (entry.getValue().contains(vmBreakpoint)) {
        myDebugProcess.getSession().updateBreakpointPresentation(entry.getKey(), Db_verified_breakpoint, null);
        return;
      }
    }
  }

  @Nullable
  public XLineBreakpoint<?> handleIsolateCreatedAndReturnBreakpointAtPosition(@NotNull final VmIsolate isolate,
                                                                              @Nullable final VmLocation vmLocation) {
    int locationLine = vmLocation == null ? 0 : vmLocation.getLineNumber(myDebugProcess.getVmConnection());
    XLineBreakpoint<?> breakpointAtLocation = null;

    for (XLineBreakpoint<?> xBreakpoint : myXBreakpoints) {
      doRegisterBreakpoint(isolate, xBreakpoint);

      final XSourcePosition sourcePosition = xBreakpoint.getSourcePosition();
      if (sourcePosition != null &&
          vmLocation != null &&
          sourcePosition.getLine() == locationLine - 1 &&
          vmLocation.getUnescapedUrl() != null &&
          sourcePosition.getFile().equals(myDebugProcess.getDartUrlResolver().findFileByDartUrl(vmLocation.getUnescapedUrl()))) {
        breakpointAtLocation = xBreakpoint;
      }
    }

    return breakpointAtLocation;
  }

  public void handleIsolateShutdown(@NotNull final VmIsolate isolate) {
    final Iterator<? extends VmBreakpoint> iterator = myXBreakpointToVmBreakpoints.values().iterator();
    while (iterator.hasNext()) {
      if (iterator.next().getIsolate() == isolate) {
        iterator.remove();
      }
    }
  }

  @Nullable
  public XLineBreakpoint<?> getBreakpointForLocation(@Nullable final VmLocation vmLocation) {
    if (vmLocation == null) return null;

    for (Map.Entry<XLineBreakpoint<?>, Collection<VmBreakpoint>> entry : myXBreakpointToVmBreakpoints.entrySet()) {
      final XLineBreakpoint<?> xBreakpoint = entry.getKey();
      final Collection<VmBreakpoint> vmBreakpoints = entry.getValue();
      for (VmBreakpoint vmBreakpoint : vmBreakpoints) {
        final VmLocation loc = vmBreakpoint.getLocation();
        if (loc != null && loc.getTokenOffset() == vmLocation.getTokenOffset() && loc.getUrl().equals(vmLocation.getUrl())) {
          return xBreakpoint;
        }
      }
    }

    return null;
  }
}



