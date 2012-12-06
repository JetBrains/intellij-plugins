package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.ide.runner.base.DartBreakpointType;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartiumBreakPointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {
  private final XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> myHandler;

  public DartiumBreakPointHandler(XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> handler) {
    super(DartBreakpointType.class);
    myHandler = handler;
  }

  @Override
  public void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myHandler.registerBreakpoint(breakpoint);
  }

  @Override
  public void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, boolean temporary) {
    myHandler.unregisterBreakpoint(breakpoint, temporary);
  }
}
