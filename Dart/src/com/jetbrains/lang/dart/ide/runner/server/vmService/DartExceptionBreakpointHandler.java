package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.jetbrains.lang.dart.ide.runner.DartExceptionBreakpointProperties;
import com.jetbrains.lang.dart.ide.runner.DartExceptionBreakpointType;
import org.dartlang.vm.service.element.ExceptionPauseMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartExceptionBreakpointHandler extends XBreakpointHandler<XBreakpoint<DartExceptionBreakpointProperties>> {

  private final DartVmServiceDebugProcess myDebugProcess;

  public DartExceptionBreakpointHandler(@NotNull final DartVmServiceDebugProcess debugProcess) {
    super(DartExceptionBreakpointType.class);
    myDebugProcess = debugProcess;
  }

  @NotNull
  public static XBreakpoint<DartExceptionBreakpointProperties> getDefaultExceptionBreakpoint(@NotNull final Project project) {
    final XBreakpointManager bpManager = XDebuggerManager.getInstance(project).getBreakpointManager();
    final DartExceptionBreakpointType bpType = XBreakpointType.EXTENSION_POINT_NAME.findExtension(DartExceptionBreakpointType.class);
    final XBreakpoint<DartExceptionBreakpointProperties> breakpoint = bpManager.getDefaultBreakpoint(bpType);
    assert breakpoint != null;
    return breakpoint;
  }

  @NotNull
  public static ExceptionPauseMode getBreakOnExceptionMode(@NotNull XDebugSession session,
                                                           @Nullable final XBreakpoint<DartExceptionBreakpointProperties> bp) {
    if (session.areBreakpointsMuted()) {
      return ExceptionPauseMode.None;
    }
    if (bp == null) return ExceptionPauseMode.Unhandled; // Default to breaking on unhandled exceptions.
    if (!bp.isEnabled()) return ExceptionPauseMode.None;
    return bp.getProperties().isBreakOnAllExceptions() ? ExceptionPauseMode.All : ExceptionPauseMode.Unhandled;
  }

  @Override
  public void registerBreakpoint(@NotNull final XBreakpoint<DartExceptionBreakpointProperties> breakpoint) {
    final VmServiceWrapper vmServiceWrapper = myDebugProcess.getVmServiceWrapper();
    if (vmServiceWrapper != null) {
      vmServiceWrapper.setExceptionPauseMode(getBreakOnExceptionMode(myDebugProcess.getSession(), breakpoint));
    }
  }

  @Override
  public void unregisterBreakpoint(@NotNull final XBreakpoint<DartExceptionBreakpointProperties> breakpoint, final boolean temporary) {
    registerBreakpoint(breakpoint);
  }
}
