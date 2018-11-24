package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.util.ArrayUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerLog;
import com.jetbrains.cidr.execution.debugger.IPhoneDebugProcess;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrBreakpointHandler;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrExceptionBreakpointHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyAbstractCommandLineState;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionDeviceDebugProcess extends IPhoneDebugProcess {
  @NotNull private final RunProfileState myState;
  @NotNull private final Executor myExecutor;
  @NotNull private final ProcessHandler myProcessHandler;

  public RubyMotionDeviceDebugProcess(@NotNull XDebugSession session,
                                      @NotNull RunProfileState state,
                                      @NotNull Executor executor,
                                      @NotNull TextConsoleBuilder consoleBuilder,
                                      @NotNull ProcessHandler processHandler) throws ExecutionException {
    super(new MotionAppRunParameters(true, processHandler), MotionAppRunParameters.getDevice(processHandler), session, consoleBuilder);
    myState = state;
    myExecutor = executor;
    myProcessHandler = processHandler;
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
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    final XBreakpointHandler<?>[] handlers = super.getBreakpointHandlers();
    return ArrayUtil.remove(handlers, 3);
  }

  @NotNull
  @Override
  public ConsoleView createConsole() {
    myConsole.attachToProcess(myProcessHandler);
    try {
      if (myState instanceof RubyAbstractCommandLineState) {
        return ((RubyAbstractCommandLineState)myState).createAndAttachConsole(getSession().getProject(), getProcessHandler(), myExecutor);
      }
    } catch (ExecutionException e) {
      CidrDebuggerLog.LOG.info("Error while creating console: " + e);
    }
    return myConsole;
  }
}
