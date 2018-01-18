/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerLog;
import com.jetbrains.cidr.execution.debugger.IPhoneSimulatorDebugProcess;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrBreakpointHandler;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrExceptionBreakpointHandler;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrSymbolicBreakpointHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyAbstractCommandLineState;

import java.util.List;

/**
* @author Dennis.Ushakov
*/
public abstract class RubyMotionSimulatorDebugProcess extends IPhoneSimulatorDebugProcess {
  private final RunProfileState myState;
  private final Executor myExecutor;
  private final ProcessHandler myServerProcessHandler;

  public RubyMotionSimulatorDebugProcess(XDebugSession session,
                                         RunProfileState state,
                                         Executor executor, TextConsoleBuilder consoleBuilder,
                                         ProcessHandler serverProcessHandler)
    throws ExecutionException {
    super(new MotionAppRunParameters(false, serverProcessHandler), session, consoleBuilder, false);
    myState = state;
    myExecutor = executor;
    myServerProcessHandler = serverProcessHandler;
  }

  @NotNull
  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    final List<XBreakpointHandler<?>> handlerList = ContainerUtil.filter(super.getBreakpointHandlers(),
                                                                         handler -> !(handler instanceof CidrSymbolicBreakpointHandler));
    return handlerList.toArray(XBreakpointHandler.EMPTY_ARRAY);
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