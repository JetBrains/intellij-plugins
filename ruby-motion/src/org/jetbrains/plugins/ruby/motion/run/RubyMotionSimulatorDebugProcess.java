package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.util.ArrayUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerLog;
import com.jetbrains.cidr.execution.debugger.IPhoneSimulatorDebugProcessBase;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrBreakpointHandler;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrExceptionBreakpointHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyAbstractCommandLineState;

/**
* @author Dennis.Ushakov
*/
public abstract class RubyMotionSimulatorDebugProcess extends IPhoneSimulatorDebugProcessBase {
  private final RunProfileState myState;
  private final Executor myExecutor;
  private final ProcessHandler myServerProcessHandler;

  public RubyMotionSimulatorDebugProcess(XDebugSession session,
                                         RunProfileState state,
                                         Executor executor, TextConsoleBuilder consoleBuilder,
                                         ProcessHandler serverProcessHandler)
    throws ExecutionException {
    super(new MotionAppRunParameters(false, serverProcessHandler), session, consoleBuilder);
    myState = state;
    myExecutor = executor;
    myServerProcessHandler = serverProcessHandler;
  }

  @NotNull
  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    final XBreakpointHandler<?>[] handlers = super.getBreakpointHandlers();
    return ArrayUtil.remove(handlers, 3);
  }

  @NotNull
  @Override
  protected CidrBreakpointHandler createBreakpointHandler() {
    return new CidrBreakpointHandler(this, MotionLineBreakpointType.class);
  }

  @NotNull
  @Override
  protected CidrExceptionBreakpointHandler createExceptionHandler() {
    return new CidrExceptionBreakpointHandler(this, MotionExceptionBreakpointType.class);
  }

  @NotNull
  @Override
  public ConsoleView createConsole() {
    myConsole.attachToProcess(myServerProcessHandler);
    try {
      if (myState instanceof RubyAbstractCommandLineState) {
        return ((RubyAbstractCommandLineState)myState).createAndAttachConsole(getSession().getProject(), getProcessHandler(), myExecutor);
      }
    }
    catch (ExecutionException e) {
      CidrDebuggerLog.LOG.info("Error while creating console: " + e);
    }
    return myConsole;
  }

  @Override
  public boolean isDetachDefault() {
    if (!(myState instanceof RubyAbstractCommandLineState)) {
      return super.isDetachDefault();
    }
    final Module module = ((RubyAbstractCommandLineState)myState).getConfig().getModule();
    assert module != null;
    return !RubyMotionUtil.getInstance().isOSX(module);
  }
}