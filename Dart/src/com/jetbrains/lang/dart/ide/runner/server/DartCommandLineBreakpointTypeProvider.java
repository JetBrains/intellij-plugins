package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

/**
 * Created by fedorkorotkov.
 */
public interface DartCommandLineBreakpointTypeProvider {
  ExtensionPointName<DartCommandLineBreakpointTypeProvider> EP_NAME =
    ExtensionPointName.create("com.jetbrains.lang.dart.debugger.breakpoint.class.provider");

  Class<? extends XBreakpointType<XLineBreakpoint<XBreakpointProperties>, ?>> provideBreakpointClass();
}
