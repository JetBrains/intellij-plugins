package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.jetbrains.cidr.execution.deviceSupport.AMDevice;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public interface DeviceProcessHandler {
  @NotNull String getRemotePath() throws ExecutionException;
  @NotNull String getLocalPath() throws ExecutionException;
  @NotNull String getWorkingDirectory();
  @NotNull AMDevice getDevice() throws ExecutionException;
  @NotNull Map<String,String> getEnvironment();
}
