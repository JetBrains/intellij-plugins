package com.jetbrains.lang.dart.ide.runner.base;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineBreakpointTypeProvider;

/**
 * Created by fedorkorotkov.
 */
public class DartBreakpointTypeProvider implements DartCommandLineBreakpointTypeProvider {
  @Override
  public Class<? extends XBreakpointType<XLineBreakpoint<XBreakpointProperties>, ?>> provideBreakpointClass() {
    return DartBreakpointType.class;
  }
}
