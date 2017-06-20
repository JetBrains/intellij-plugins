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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.jetbrains.cidr.execution.ExecutionResult;
import com.jetbrains.cidr.execution.ProcessHandlerWithPID;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.ruby.run.RubyProcessHandler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Dennis.Ushakov
 */
public class MotionProcessHandler extends RubyProcessHandler implements ProcessHandlerWithPID, ProcessHandlerWithDetachSemaphore {
  private final ExecutionResult<Integer> myPIDResult = new ExecutionResult<>();
  private final MotionSimulatorRunExtension.MotionProcessOutputReaders myReaders;
  private boolean mySimulateStarted = false;
  private final boolean isOSX;
  private Semaphore myDetachSemaphore;

  protected MotionProcessHandler(@NotNull final Module module, @NotNull Process process,
                                 @NotNull String commandLine,
                                 @Nullable String runnerId,
                                 @NotNull MotionSimulatorRunExtension.MotionProcessOutputReaders readers) {
    super(process, commandLine, RubyProcessHandler.getOutputEncoding(), runnerId);
    myReaders = readers;
    myReaders.setHandler(this);
    isOSX = RubyMotionUtil.getInstance().isOSX(module);
  }

  @Override
  protected void detachProcessImpl() {
    super.detachProcessImpl();
    myReaders.close();
  }

  @Override
  protected void destroyProcessImpl() {
    try {
      if (myDetachSemaphore != null) myDetachSemaphore.acquire();
    }
    catch (InterruptedException e) {
      CidrDebuggerLog.LOG.info(e);
    }
    super.destroyProcessImpl();
    myReaders.close();
  }

  @Override
  public void coloredTextAvailable(@NotNull String text, @NotNull Key attributes) {
    mySimulateStarted |= text.contains(isOSX ? "Run" : "Simulate");
    final String[] lines = text.split("\r");
    SimulatorConsoleProcessHandler.findPid(lines, mySimulateStarted, myPIDResult);
    super.coloredTextAvailable(text, attributes);
  }

  @Override
  public int getPID() throws ExecutionException {
    return myPIDResult.get();
  }

  @Override
  public int getPID(long timeout) throws ExecutionException, TimeoutException {
    return myPIDResult.get(timeout, TimeUnit.MILLISECONDS);
  }

  @Override
  public void setDetachSemaphore(Semaphore detachSemaphore) {
    myDetachSemaphore = detachSemaphore;
  }
}
