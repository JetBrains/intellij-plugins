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
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.RunParameters;
import com.jetbrains.cidr.execution.TrivialInstaller;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerLog;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.XcodeLLDBDriverConfiguration;
import com.jetbrains.cidr.execution.deviceSupport.AMDevice;
import com.jetbrains.cidr.execution.deviceSupport.AMDeviceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
class MotionAppRunParameters extends RunParameters {
  private final boolean myOnDevice;
  private final ProcessHandler myServerProcessHandler;

  public MotionAppRunParameters(boolean onDevice, ProcessHandler serverProcessHandler) {
    myOnDevice = onDevice;
    myServerProcessHandler = serverProcessHandler;
  }

  @NotNull
  @Override
  public Installer getInstaller() {
    return myOnDevice ? new MotionInstaller((DeviceProcessHandler)myServerProcessHandler) :
           new TrivialInstaller(new GeneralCommandLine(""));
  }

  @Nullable
  @Override
  public String getArchitectureId() {
    return myOnDevice ? AMDeviceManager.getInstance().getDeviceArchitecture(getDevice()) : "i386";
  }

  public AMDevice getDevice() {
    return getDevice(myServerProcessHandler);
  }

  @NotNull
  public static AMDevice getDevice(ProcessHandler serverProcessHandler) {
    try {
      return ((DeviceProcessHandler)serverProcessHandler).getDevice();
    }
    catch (ExecutionException e) {
      CidrDebuggerLog.LOG.error(e);
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  public DebuggerDriverConfiguration getDebuggerDriverConfiguration() {
    return new XcodeLLDBDriverConfiguration(null);
  }
}
