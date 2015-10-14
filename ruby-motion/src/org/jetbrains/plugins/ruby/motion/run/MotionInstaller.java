package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.deviceSupport.AMDevice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Dennis.Ushakov
 */
class MotionInstaller implements Installer {
  private final DeviceProcessHandler myHandler;

  public MotionInstaller(DeviceProcessHandler handler) {
    myHandler = handler;
  }

  @NotNull
  @Override
  public GeneralCommandLine install() throws ExecutionException {
    final GeneralCommandLine result = new GeneralCommandLine();
    result.setExePath(myHandler.getRemotePath());
    result.setWorkDirectory(myHandler.getWorkingDirectory());
    result.getEnvironment().putAll(myHandler.getEnvironment());
    final AMDevice device = myHandler.getDevice();
    try {
      device.lock();
      device.connect();
      result.putUserData(DebuggerDriver.DEBUGSERVER_SOCKET, device.startDebugserver());
    }
    finally {
      device.disconnect();
    }

    result.setCharset(EncodingManager.getInstance().getDefaultCharset());
    return result;
  }

  @NotNull
  @Override
  public File getExecutableFile() {
    try {
      return new File(myHandler.getLocalPath());
    }
    catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public File getAppWorkingDir() {
    String result = myHandler.getWorkingDirectory();
    return StringUtil.isEmptyOrSpaces(result) ? null : new File(result);
  }
}
