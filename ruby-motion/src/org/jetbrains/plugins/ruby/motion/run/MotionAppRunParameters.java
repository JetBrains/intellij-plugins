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

  @Override
  public boolean isWaitFor() {
    return false;
  }

  @Nullable
  @Override
  public String getArchitectureId() {
    return myOnDevice ? AMDeviceManager.getInstance().getDeviceArchitecture(getDevice()) : "i386";
  }

  public AMDevice getDevice() {
    try {
      return ((DeviceProcessHandler)myServerProcessHandler).getDevice();
    }
    catch (ExecutionException e) {
      CidrDebuggerLog.LOG.error(e);
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  public DebuggerDriverConfiguration getDebuggerDriverConfiguration() {
    return new XcodeLLDBDriverConfiguration();
  }
}
