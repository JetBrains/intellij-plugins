package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.util.ArrayUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerLog;
import com.jetbrains.cidr.execution.debugger.IPhoneSimulatorDebugProcessBase;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrBreakpointHandler;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrExceptionBreakpointHandler;
import com.jetbrains.cidr.execution.deviceSupport.AMDevice;
import com.jetbrains.cidr.execution.deviceSupport.AMDeviceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyAbstractCommandLineState;

/**
 * @author Dennis.Ushakov
 */
public abstract class RubyMotionDeviceDebugProcess extends IPhoneSimulatorDebugProcessBase {
  @NotNull private final RunProfileState myState;
  @NotNull private final Executor myExecutor;
  @NotNull private final ProcessHandler myProcessHandler;

  public RubyMotionDeviceDebugProcess(@NotNull XDebugSession session,
                                      @NotNull RunProfileState state,
                                      @NotNull Executor executor,
                                      @NotNull TextConsoleBuilder consoleBuilder,
                                      @NotNull ProcessHandler processHandler) throws ExecutionException {
    super(new MotionAppRunParameters(true, processHandler), session, consoleBuilder);
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

  @Override
  protected void doLaunchTarget(@NotNull DebuggerDriver driver) throws ExecutionException {
    driver.setRedirectOutputToFiles(!isRemote());
    driver.launch();
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

  @Override
  protected boolean isRemote() {
    return true;
  }

  @Override
  public boolean supportsWatchpoints() {
    return false;
  }

  @Override
  protected void doStart(@NotNull DebuggerDriver driver) throws ExecutionException {
    final AMDevice device = getDevice();
    myProcessHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        getDevice().stopDebugserver();
        getDevice().unlock();
      }
    });

    driver.loadForRemote(AMDeviceManager.findDeviceSupportDirectory(device));
  }

  private AMDevice getDevice() {
    return ((MotionAppRunParameters)myRunParameters).getDevice();
  }
}
