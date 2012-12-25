package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.chromeConnector.debugger.ChromeDebugProcess;
import com.intellij.chromeConnector.extension.ExtBackedChromeConnection;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public class DartiumDebugProcess extends ChromeDebugProcess {

  public DartiumDebugProcess(@NotNull XDebugSession session,
                             @NotNull DebuggableFileFinder finder,
                             @NotNull ExtBackedChromeConnection connection,
                             @Nullable String initialUrl) {
    super(session, finder, connection, initialUrl, null);

    final XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> chromeHandler =
      (XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>)getBreakpointHandlers()[0];
    getBreakpointHandlers()[0] = new DartiumBreakPointHandler(chromeHandler);
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new DartDebuggerEditorsProvider();
  }
}
