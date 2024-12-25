// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

import java.util.Set;

public class DartExceptionBreakpointHandler extends XBreakpointHandler<XBreakpoint<DartExceptionBreakpointProperties>> {

  private final DartVmServiceDebugProcess myDebugProcess;

  public DartExceptionBreakpointHandler(final @NotNull DartVmServiceDebugProcess debugProcess) {
    super(DartExceptionBreakpointType.class);
    myDebugProcess = debugProcess;
  }

  public static @NotNull XBreakpoint<DartExceptionBreakpointProperties> getDefaultExceptionBreakpoint(final @NotNull Project project) {
    final XBreakpointManager bpManager = XDebuggerManager.getInstance(project).getBreakpointManager();
    final DartExceptionBreakpointType bpType = XBreakpointType.EXTENSION_POINT_NAME.findExtension(DartExceptionBreakpointType.class);
    assert bpType != null;
    Set<XBreakpoint<DartExceptionBreakpointProperties>> breakpoints = bpManager.getDefaultBreakpoints(bpType);
    assert breakpoints.size() == 1 : breakpoints;
    return breakpoints.iterator().next();
  }

  public static @NotNull ExceptionPauseMode getBreakOnExceptionMode(@NotNull XDebugSession session,
                                                                    final @Nullable XBreakpoint<DartExceptionBreakpointProperties> bp) {
    if (session.areBreakpointsMuted()) {
      return ExceptionPauseMode.None;
    }
    if (bp == null) return ExceptionPauseMode.Unhandled; // Default to breaking on unhandled exceptions.
    if (!bp.isEnabled()) return ExceptionPauseMode.None;
    return bp.getProperties().isBreakOnAllExceptions() ? ExceptionPauseMode.All : ExceptionPauseMode.Unhandled;
  }

  @Override
  public void registerBreakpoint(final @NotNull XBreakpoint<DartExceptionBreakpointProperties> breakpoint) {
    final VmServiceWrapper vmServiceWrapper = myDebugProcess.getVmServiceWrapper();
    if (vmServiceWrapper != null) {
      vmServiceWrapper.setExceptionPauseMode(getBreakOnExceptionMode(myDebugProcess.getSession(), breakpoint));
    }
  }

  @Override
  public void unregisterBreakpoint(final @NotNull XBreakpoint<DartExceptionBreakpointProperties> breakpoint, final boolean temporary) {
    registerBreakpoint(breakpoint);
  }
}
